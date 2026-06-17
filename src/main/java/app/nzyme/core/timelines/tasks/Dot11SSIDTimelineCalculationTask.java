package app.nzyme.core.timelines.tasks;

import app.nzyme.plugin.distributed.tasksqueue.Task;
import app.nzyme.plugin.distributed.tasksqueue.TaskType;

import java.util.Map;

public class Dot11SSIDTimelineCalculationTask extends Task {

    @Override
    public TaskType type() {
        return TaskType.TIMELINES_CALCULATION_DOT11_SSID;
    }

    @Override
    public boolean allowProcessSelf() {
        return true;
    }

    @Override
    public Map<String, Object> parameters() {
        return Map.of();
    }

    @Override
    public boolean allowRetry() {
        return false;
    }

}
