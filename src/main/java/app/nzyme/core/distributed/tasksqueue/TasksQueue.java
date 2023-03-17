package app.nzyme.core.distributed.tasksqueue;

import java.util.List;

public interface TasksQueue {

    void publish(Task task);
    List<Task> poll();
    void retry(long taskId);

}
