package app.nzyme.core.distributed.tasksqueue;

public interface TaskHandler {

    TaskProcessingResult handle(ReceivedTask task);

    String getName();

}
