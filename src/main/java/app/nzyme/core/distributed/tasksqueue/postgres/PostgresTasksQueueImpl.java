package app.nzyme.core.distributed.tasksqueue.postgres;

import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.distributed.tasksqueue.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
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
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("psql-tasks-poller-%d")
                .build()
        ).scheduleWithFixedDelay(this::poll, pollInterval, pollInterval, pollIntervalUnit);

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("psql-tasks-retention-cleaner-%d")
                .build()
        ).scheduleAtFixedRate(() -> retentionClean(DateTime.now().minusDays(7)),
                1, 1, TimeUnit.HOURS);

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
                                "created_at, status, previous_status, retries, processing_time_ms, allow_process_self) " +
                                "VALUES(:sender_node_id, :type, :allow_retry, :parameters, :created_at, :status, " +
                                ":previous_status, :retries, :processing_time_ms, :allow_process_self)")
                        .bind("sender_node_id", nzyme.getNodeInformation().id())
                        .bind("type", task.type().name())
                        .bind("allow_retry", task.allowRetry())
                        .bind("parameters", parameters)
                        .bind("created_at", DateTime.now())
                        .bind("status", TaskStatus.NEW)
                        .bind("previous_status", TaskStatus.NEW)
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
                    handle.createQuery("UPDATE tasks_queue SET status = 'ACK', previous_status = status, " +
                                    "last_acked_at = :timestamp WHERE status IN ('NEW', 'NEW_RETRY') " +
                                    "AND (allow_process_self = true " +
                                    "OR (allow_process_self = false AND sender_node_id != :own_node_id)) RETURNING *")
                            .bind("own_node_id", nzyme.getNodeInformation().id())
                            .bind("timestamp", DateTime.now())
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

                TaskStatus previousStatus;
                try {
                    previousStatus = TaskStatus.valueOf(task.previousStatus());
                } catch (IllegalArgumentException e) {
                    LOG.warn("Unsupported task status [{}]. Skipping.", task.previousStatus());
                    continue;
                }

                DateTime timestamp = DateTime.now();

                // Send to registered handlers.
                if (taskHandlers.containsKey(type)) {
                    for (TaskHandler handler : taskHandlers.get(type)) {
                        if (previousStatus.equals(TaskStatus.NEW_RETRY)) {
                            incrementRetryCount(task.id());
                        }

                        Map<String, Object> serializedParameters = this.om.readValue(
                                task.parameters(),
                                new TypeReference<HashMap<String, Object>>() {
                                }
                        );

                        Stopwatch stopwatch = Stopwatch.createStarted();
                        TaskProcessingResult opResult = handler.handle(ReceivedTask.create(
                                type,
                                task.senderNodeId(),
                                task.allowProcessSelf(),
                                serializedParameters,
                                task.parameters(),
                                task.allowRetry()
                        ));
                        long tookMs = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                        if (tookMs == 0) {
                            tookMs = 1;
                        }

                        if (opResult.equals(TaskProcessingResult.FAILURE)) {
                            LOG.error("Could not execute cluster task <#{}> of type [{}]. Marking as failure.",
                                    task.id(), task.type());
                        }

                        setTaskStatus(task.id(),
                                opResult == TaskProcessingResult.SUCCESS
                                        ? TaskStatus.PROCESSED_SUCCESS : TaskStatus.PROCESSED_FAILURE
                        );

                        if (previousStatus.equals(TaskStatus.NEW)) {
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

    private void incrementRetryCount(long taskId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE tasks_queue SET retries = retries+1 WHERE id = :id")
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
        long eligibleCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM tasks_queue WHERE allow_retry = true " +
                                "AND status = :status AND id = :id")
                        .bind("id", taskId)
                        .bind("status", TaskStatus.PROCESSED_FAILURE)
                        .mapTo(Long.class)
                        .first()
        );

        if (eligibleCount != 1) {
            LOG.warn("Task ID <{}> does not exist or is not allowed to be retried.", taskId);
            return;
        }

        setTaskStatus(taskId, TaskStatus.NEW_RETRY);
    }

    @Override
    public List<StoredTask> getAllFailedTasksSince(DateTime since) {
        List<PostgresTasksQueueEntry> failures = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM tasks_queue WHERE status = :status AND created_at > :since " +
                                "ORDER BY created_at DESC")
                        .bind("status", TaskStatus.PROCESSED_FAILURE)
                        .bind("since", since)
                        .mapTo(PostgresTasksQueueEntry.class)
                        .list()
        );


        return entriesToStoredTasks(failures);
    }

    @Override
    public List<StoredTask> getAllStuckTasks(DateTime timeout) {
        List<PostgresTasksQueueEntry> failures = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM tasks_queue WHERE status = :status AND last_acked_at < :timeout " +
                                "ORDER BY created_at DESC")
                        .bind("status", TaskStatus.ACK)
                        .bind("timeout", timeout)
                        .mapTo(PostgresTasksQueueEntry.class)
                        .list()
        );


        return entriesToStoredTasks(failures);
    }

    @Override
    public List<StoredTask> getAllTasks(int limit, int offset) {
        List<PostgresTasksQueueEntry> entries = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM tasks_queue ORDER BY created_at DESC " +
                                "LIMIT :limit OFFSET :offset ")
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(PostgresTasksQueueEntry.class)
                        .list()
        );

        return entriesToStoredTasks(entries);
    }

    @Override
    public void onMessageReceived(TaskType type, TaskHandler taskHandler) {
        LOG.debug("Registering task queue handler [{}] for type [{}]", taskHandler.getName(), type);

        if (!taskHandlers.containsKey(type)) {
            taskHandlers.put(type, Lists.newArrayList());
        }

        taskHandlers.get(type).add(taskHandler);
    }

    public void retentionClean(DateTime cutoff) {
        LOG.info("Running retention cleaning for tasks queue.");
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM tasks_queue WHERE created_at < :timeout")
                        .bind("timeout", cutoff)
                        .execute()
        );
    }

    private List<StoredTask> entriesToStoredTasks(List<PostgresTasksQueueEntry> entries) {
        List<StoredTask> result = Lists.newArrayList();
        for (PostgresTasksQueueEntry entry : entries) {
            Map<String, Object> serializedParameters = null;
            try {
                serializedParameters = this.om.readValue(
                        entry.parameters(), new TypeReference<HashMap<String, Object>>() {}
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not serialize parameters of task <" + entry.id() + ">.", e);
            }

            result.add(StoredTask.create(
                    entry.id(),
                    entry.senderNodeId(),
                    TaskType.valueOf(entry.type()),
                    entry.allowRetry(),
                    serializedParameters,
                    entry.createdAt(),
                    TaskStatus.valueOf(entry.status()),
                    entry.retries(),
                    entry.allowProcessSelf(),
                    Strings.isNullOrEmpty(entry.previousStatus()) ? null : TaskStatus.valueOf(entry.previousStatus()),
                    entry.processingTimeMs(),
                    entry.firstProcessedAt(),
                    entry.lastProcessedAt()
            ));
        }

        return result;
    }

    @Override
    public long getTotalTaskCount() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM tasks_queue")
                        .mapTo(Long.class)
                        .one()
        );
    }

}