package app.nzyme.core.distributed.tasksqueue.postgres;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.tasksqueue.Task;
import app.nzyme.core.distributed.tasksqueue.TaskHandler;
import app.nzyme.core.distributed.tasksqueue.TaskProcessingResult;
import app.nzyme.core.distributed.tasksqueue.TaskType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

public class PostgresTasksQueueImplTest {

    /*
     * tests:
     *   - sets to NEW_RETRY on retry, increases retries
     *   - does not retry if allow_retry false
     *   - allow_process_self (multiple nodes)
     *   - retention cleaning
     */

    @BeforeMethod
    public void clean() {
        MockNzyme nzyme = new MockNzyme();

        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("TRUNCATE nodes").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("TRUNCATE tasks_queue").execute());
    }

    @Test
    public void testPublishSuccess() {
        MockNzyme nzyme = new MockNzyme(0, Integer.MAX_VALUE, TimeUnit.DAYS);
        PostgresTasksQueueImpl tq = (PostgresTasksQueueImpl) nzyme.getTasksQueue();

        assertEquals(countTotalTasks(nzyme), 0);

        AtomicInteger calls = new AtomicInteger(0);

        tq.onMessageReceived(TaskType.TEST, new TaskHandler() {
            @Override
            public TaskProcessingResult handle(Task task) {
                long notAcked = nzyme.getDatabase().withHandle(handle ->
                        handle.createQuery("SELECT COUNT(*) FROM tasks_queue WHERE status != 'ACK'")
                                .mapTo(Long.class)
                                .one()
                );
                assertEquals(notAcked, 0);

                try {
                    // Generate a positive processing_time value
                    Thread.sleep(25);
                } catch(InterruptedException ignored) {}

                calls.incrementAndGet();
                return TaskProcessingResult.SUCCESS;
            }

            @Override
            public String getName() {
                return null;
            }
        });

        tq.publish(Task.create(
                TaskType.TEST,
                true,
                Collections.emptyMap(),
                true
        ));

        assertEquals(countTotalTasks(nzyme), 1);

        assertEquals(calls.get(), 0);
        tq.poll();
        assertEquals(calls.get(), 1);

        long notSuccess = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM tasks_queue WHERE status != 'PROCESSED_SUCCESS'")
                        .mapTo(Long.class)
                        .one()
        );
        assertEquals(notSuccess, 0);

        int processingTime = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT processing_time_ms FROM tasks_queue LIMIT 1")
                        .mapTo(Integer.class)
                        .one()
        );
        assertTrue(processingTime > 0);
    }

    @Test
    public void testPublishFailure() {
        MockNzyme nzyme = new MockNzyme(0, Integer.MAX_VALUE, TimeUnit.DAYS);
        PostgresTasksQueueImpl tq = (PostgresTasksQueueImpl) nzyme.getTasksQueue();

        assertEquals(countTotalTasks(nzyme), 0);

        AtomicInteger calls = new AtomicInteger(0);

        tq.onMessageReceived(TaskType.TEST, new TaskHandler() {
            @Override
            public TaskProcessingResult handle(Task task) {
                long notAcked = nzyme.getDatabase().withHandle(handle ->
                        handle.createQuery("SELECT COUNT(*) FROM tasks_queue WHERE status != 'ACK'")
                                .mapTo(Long.class)
                                .one()
                );
                assertEquals(notAcked, 0);

                try {
                    // Generate a positive processing_time value
                    Thread.sleep(25);
                } catch(InterruptedException ignored) {}

                calls.incrementAndGet();
                return TaskProcessingResult.FAILURE;
            }

            @Override
            public String getName() {
                return null;
            }
        });

        tq.publish(Task.create(
                TaskType.TEST,
                true,
                Collections.emptyMap(),
                true
        ));

        assertEquals(countTotalTasks(nzyme), 1);

        assertEquals(calls.get(), 0);
        tq.poll();
        assertEquals(calls.get(), 1);

        long notFailure = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM tasks_queue WHERE status != 'PROCESSED_FAILURE'")
                        .mapTo(Long.class)
                        .one()
        );
        assertEquals(notFailure, 0);

        int processingTime = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT processing_time_ms FROM tasks_queue LIMIT 1")
                        .mapTo(Integer.class)
                        .one()
        );
        assertTrue(processingTime > 0);
    }

    private long countTotalTasks(NzymeNode nzyme) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM tasks_queue")
                        .mapTo(Long.class)
                        .one()
        );
    }

}