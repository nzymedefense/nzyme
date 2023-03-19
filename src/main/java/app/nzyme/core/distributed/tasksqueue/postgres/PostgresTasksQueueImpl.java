package app.nzyme.core.distributed.tasksqueue.postgres;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.tasksqueue.*;
import app.nzyme.plugin.distributed.messaging.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PostgresTasksQueueImpl implements TasksQueue {

    private static final Logger LOG = LogManager.getLogger(PostgresTasksQueueImpl.class);

    public NzymeNode nzyme;
    private final ObjectMapper om;

    private final Map<TaskType, List<TaskHandler>> taskHandlers;

    private boolean initialized;

    public PostgresTasksQueueImpl(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.initialized = false;

        this.taskHandlers = Maps.newConcurrentMap();

        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void initialize() {
        initialize(5, TimeUnit.SECONDS);
    }

    public void initialize(int pollInterval, TimeUnit pollIntervalUnit) {
        this.initialized = true;
    }

    @Override
    public void publish(Task task) {
        if (!initialized) {
            throw new RuntimeException("Tasks queue is not initialized.");
        }

        LOG.debug("Publishing task [{}]", task);

        String parameters;
        try {
            parameters = om.writeValueAsString(task.parameters());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize task parameters.", e);
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO tasks_queue(sender_node_id, type, allow_retry, parameters, " +
                                "created_at, status, retries, processing_time_ms, allow_process_self) " +
                                "VALUES(:sender_node_id, :type, :allow_retry, :parameters, :created_at, :status, " +
                                ":retries, :processing_time_ms, :allow_process_self)")
                        .bind("sender_node_id", nzyme.getNodeInformation().id())
                        .bind("type", task.type().name())
                        .bind("allow_retry", task.allowRetry())
                        .bind("parameters", parameters)
                        .bind("created_at", DateTime.now())
                        .bind("status", TaskStatus.NEW)
                        .bind("retries", 0)
                        .bind("processing_time_ms", 0)
                        .bind("allow_process_self", task.allowProcessSelf())
                        .execute()
        );
    }

    @Override
    public void poll() {
        try {
            List<PostgresTasksQueueEntry> tasks = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("UPDATE tasks_queue SET status = 'ACK' " +
                                    "WHERE status IN ('NEW', 'NEW_RETRY') " +
                                    "AND (allow_process_self = true " +
                                    "OR (allow_process_self = false AND sender_node_id != :own_node_id)) RETURNING *")
                            .bind("own_node_id", nzyme.getNodeInformation().id())
                            .mapTo(PostgresTasksQueueEntry.class)
                            .list()
            );

            if (tasks.isEmpty()) {
                LOG.debug("No tasks polled.");
                return;
            }

            for (PostgresTasksQueueEntry task : tasks) {
                LOG.debug("Polled task from bus: [{}]", task);

                TaskType type;
                try {
                    type = TaskType.valueOf(task.type());
                } catch (IllegalArgumentException e) {
                    LOG.warn("Unsupported task type [{}]. Skipping.", task.type());
                    continue;
                }

                TaskStatus status;
                try {
                    status = TaskStatus.valueOf(task.status());
                } catch (IllegalArgumentException e) {
                    LOG.warn("Unsupported task status [{}]. Skipping.", task.status());
                    continue;
                }

                DateTime timestamp = DateTime.now();

                // Send to registered handlers.
                if (taskHandlers.containsKey(type)) {
                    for (TaskHandler handler : taskHandlers.get(type)) {
                        Map<String, Object> parameters = this.om.readValue(
                                task.parameters(),
                                new TypeReference<HashMap<String, Object>>() {
                                }
                        );

                        Stopwatch stopwatch = Stopwatch.createStarted();
                        TaskProcessingResult opResult = handler.handle(Task.create(
                                type, task.allowProcessSelf(), parameters, task.allowRetry()
                        ));
                        long tookMs = stopwatch.elapsed(TimeUnit.MILLISECONDS);

                        setTaskStatus(task.id(),
                                opResult == TaskProcessingResult.SUCCESS
                                        ? TaskStatus.PROCESSED_SUCCESS : TaskStatus.PROCESSED_FAILURE
                        );

                        if (status.equals(TaskStatus.NEW)) {
                            setTaskFirstProcessedAt(task.id(), timestamp);
                        }
                        setTaskPostProcessMetadata(task.id(), timestamp, (int) tookMs);
                    }
                }
            }
        } catch(Exception e) {
            LOG.error("Could not poll tasks queue.", e);
        }
    }

    private void setTaskStatus(long taskId, TaskStatus status) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE tasks_queue SET status = :status WHERE id = :id")
                        .bind("status", status.name())
                        .bind("id", taskId)
                        .execute()
        );
    }

    private void setTaskFirstProcessedAt(long taskId, DateTime timestamp) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE tasks_queue SET first_processed_at = :first_processed_at WHERE id = :id")
                        .bind("first_processed_at", timestamp)
                        .bind("id", taskId)
                        .execute()
        );
    }

    private void setTaskPostProcessMetadata(long taskId, DateTime timestamp,  int processingTimeMs) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE tasks_queue SET last_processed_at = :last_processed_at, " +
                                "processing_time_ms = :processing_time_ms WHERE id = :id")
                        .bind("last_processed_at", timestamp)
                        .bind("processing_time_ms", processingTimeMs)
                        .bind("id", taskId)
                        .execute()
        );
    }

    @Override
    public void retry(long taskId) {
    }

    @Override
    public void onMessageReceived(TaskType type, TaskHandler taskHandler) {
        LOG.debug("Registering task queue handler [{}] for type [{}]", taskHandler.getName(), type);

        if (!taskHandlers.containsKey(type)) {
            taskHandlers.put(type, Lists.newArrayList());
        }

        taskHandlers.get(type).add(taskHandler);
    }

}