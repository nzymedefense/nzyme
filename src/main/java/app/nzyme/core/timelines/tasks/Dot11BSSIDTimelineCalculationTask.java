package app.nzyme.core.timelines.tasks;

import app.nzyme.plugin.distributed.tasksqueue.Task;
import app.nzyme.plugin.distributed.tasksqueue.TaskType;

import java.util.Map;

public class Dot11BSSIDTimelineCalculationTask extends Task {

    @Override
    public TaskType type() {
        return TaskType.TIMELINES_CALCULATION_DOT11_BSSID;
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
