package app.nzyme.core.distributed.tasksqueue.postgres;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.distributed.tasksqueue.Task;
import app.nzyme.core.distributed.tasksqueue.TaskType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.Assert.*;

public class PostgresTasksQueueImplTest {

    @BeforeMethod
    public void clean() {
        MockNzyme nzyme = new MockNzyme();

        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("TRUNCATE nodes").execute());
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("TRUNCATE tasks_queue").execute());
    }

    @Test
    public void testPublish() {
        MockNzyme nzyme = new MockNzyme();
        PostgresTasksQueueImpl tq = new PostgresTasksQueueImpl(nzyme);

        tq.publish(Task.create(
                TaskType.TEST,
                false,
                Collections.emptyMap(),
                true
        ));
    }

}