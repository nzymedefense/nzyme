package app.nzyme.core.distributed.tasksqueue;

public interface TaskHandler {

    TaskProcessingResult handle(Task task);

    String getName();

}
