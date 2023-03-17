package app.nzyme.core.distributed.tasksqueue.postgres;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.tasksqueue.Task;
import app.nzyme.core.distributed.tasksqueue.TaskStatus;
import app.nzyme.core.distributed.tasksqueue.TasksQueue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;

public class PostgresTasksQueueImpl implements TasksQueue {

    private static final Logger LOG = LogManager.getLogger(PostgresTasksQueueImpl.class);

    public NzymeNode nzyme;
    private final ObjectMapper om;

    public PostgresTasksQueueImpl(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void publish(Task task) {
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
    public List<Task> poll() {
        // respect allow_process_self
        // atomically. update what it finds that's NEW or NEW_RETRY to ACK and get list of rows it affected. work on those. use SQL RETURNING?
        return null;
    }

    @Override
    public void retry(long taskId) {
    }
}