package app.nzyme.core.distributed.tasksqueue.postgres;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.distributed.tasksqueue.*;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

public class PostgresTasksQueueImplTest {
    
    @BeforeMethod
    public void clean() throws IOException {
        MockNzyme nzyme = new MockNzyme();

        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("TRUNCATE nodes").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("TRUNCATE tasks_queue").execute());

        cleanDataFolder();
    }

    private void cleanDataFolder() throws IOException {
        Path dataDir = Path.of("test_data_dir");

        Files.walk(dataDir)
                .map(Path::toFile)
                .forEach(file -> {
                    // Don't delete the entire crypto_test root directory.
                    if (!file.toPath().equals(dataDir) && !file.getName().equals(".gitkeep")) {
                        if (!file.delete()) {
                            throw new RuntimeException("Could not delete test data file [" + file.getAbsolutePath() + "] to prepare tests.");
                        }
                    }
                });

        long size = Files.walk(dataDir)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();

        assertEquals(size, 0, "Test data folder is not empty.");
    }

    @Test
    public void testPublishSuccess() throws InterruptedException {
        MockNzyme nzyme = new MockNzyme(Integer.MAX_VALUE, TimeUnit.DAYS);
        PostgresTasksQueueImpl tq = (PostgresTasksQueueImpl) nzyme.getTasksQueue();

        assertEquals(countTotalTasks(nzyme), 0);

        AtomicInteger calls = new AtomicInteger(0);

        tq.onMessageReceived(TaskType.TEST, new TaskHandler() {
            @Override
            public TaskProcessingResult handle(ReceivedTask task) {
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
        Thread.sleep(100);
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
    public void testPublishFailure() throws InterruptedException {
        MockNzyme nzyme = new MockNzyme(Integer.MAX_VALUE, TimeUnit.DAYS);
        PostgresTasksQueueImpl tq = (PostgresTasksQueueImpl) nzyme.getTasksQueue();

        assertEquals(countTotalTasks(nzyme), 0);

        AtomicInteger calls = new AtomicInteger(0);

        tq.onMessageReceived(TaskType.TEST, new TaskHandler() {
            @Override
            public TaskProcessingResult handle(ReceivedTask task) {
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
        Thread.sleep(100);
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

    @Test
    public void testMultiProducerSingleConsumer() throws IOException, InterruptedException {
        MockNzyme nzyme = new MockNzyme(Integer.MAX_VALUE, TimeUnit.DAYS);
        PostgresTasksQueueImpl tq = (PostgresTasksQueueImpl) nzyme.getTasksQueue();
        assertEquals(countTotalTasks(nzyme), 0);

        cleanDataFolder(); // This makes nzyme generate new node UUID.

        MockNzyme nzyme2 = new MockNzyme(Integer.MAX_VALUE, TimeUnit.DAYS);
        PostgresTasksQueueImpl tq2 = (PostgresTasksQueueImpl) nzyme2.getTasksQueue();
        assertEquals(countTotalTasks(nzyme2), 0);

        AtomicInteger calls = new AtomicInteger(0);
        TaskHandler th = new TaskHandler() {
            @Override
            public TaskProcessingResult handle(ReceivedTask task) {
                calls.incrementAndGet();
                return TaskProcessingResult.FAILURE;
            }

            @Override
            public String getName() {
                return null;
            }
        };

        tq.onMessageReceived(TaskType.TEST, th);
        tq2.onMessageReceived(TaskType.TEST, th);

        tq.publish(Task.create(
                TaskType.TEST,
                true,
                Collections.emptyMap(),
                true
        ));

        assertEquals(countTotalTasks(nzyme), 1);

        assertEquals(calls.get(), 0);
        tq.poll();
        tq2.poll();
        Thread.sleep(100);
        assertEquals(calls.get(), 1);
    }

    @Test
    public void testMultiProducerSingleConsumerNotAllowedToSelfProcess() throws IOException, InterruptedException {
        MockNzyme nzyme = new MockNzyme(Integer.MAX_VALUE, TimeUnit.DAYS);
        PostgresTasksQueueImpl tq = (PostgresTasksQueueImpl) nzyme.getTasksQueue();
        assertEquals(countTotalTasks(nzyme), 0);

        cleanDataFolder(); // This makes nzyme generate new node UUID.

        MockNzyme nzyme2 = new MockNzyme(Integer.MAX_VALUE, TimeUnit.DAYS);
        PostgresTasksQueueImpl tq2 = (PostgresTasksQueueImpl) nzyme2.getTasksQueue();
        assertEquals(countTotalTasks(nzyme2), 0);

        AtomicInteger calls1 = new AtomicInteger(0);
        TaskHandler th1 = new TaskHandler() {
            @Override
            public TaskProcessingResult handle(ReceivedTask task) {
                calls1.incrementAndGet();
                return TaskProcessingResult.FAILURE;
            }

            @Override
            public String getName() {
                return null;
            }
        };

        AtomicInteger calls2 = new AtomicInteger(0);
        TaskHandler th2 = new TaskHandler() {
            @Override
            public TaskProcessingResult handle(ReceivedTask task) {
                calls2.incrementAndGet();
                return TaskProcessingResult.FAILURE;
            }

            @Override
            public String getName() {
                return null;
            }
        };

        tq.onMessageReceived(TaskType.TEST, th1);
        tq2.onMessageReceived(TaskType.TEST, th2);

        tq.publish(Task.create(
                TaskType.TEST,
                false,
                Collections.emptyMap(),
                true
        ));

        assertEquals(countTotalTasks(nzyme), 1);

        assertEquals(calls1.get(), 0);
        assertEquals(calls2.get(), 0);
        tq.poll();
        tq2.poll();
        Thread.sleep(100);
        assertEquals(calls1.get(), 0);
        assertEquals(calls2.get(), 1);
    }

    @Test
    public void testRetentionCleaning() {
        MockNzyme nzyme = new MockNzyme(Integer.MAX_VALUE, TimeUnit.DAYS);
        PostgresTasksQueueImpl tq = (PostgresTasksQueueImpl) nzyme.getTasksQueue();

        assertEquals(countTotalTasks(nzyme), 0);

        tq.publish(Task.create(
                TaskType.TEST,
                true,
                Collections.emptyMap(),
                true
        ));

        tq.publish(Task.create(
                TaskType.TEST,
                true,
                Collections.emptyMap(),
                true
        ));

        tq.publish(Task.create(
                TaskType.TEST,
                true,
                Collections.emptyMap(),
                true
        ));

        assertEquals(countTotalTasks(nzyme), 3);

        tq.retentionClean(DateTime.now());

        assertEquals(countTotalTasks(nzyme), 0);
    }

    private long countTotalTasks(NzymeNode nzyme) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM tasks_queue")
                        .mapTo(Long.class)
                        .one()
        );
    }

}