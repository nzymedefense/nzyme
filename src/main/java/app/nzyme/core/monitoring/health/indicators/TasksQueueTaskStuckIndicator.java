package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.events.types.SystemEventType;
import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.plugin.distributed.tasksqueue.TasksQueue;
import org.joda.time.DateTime;

public class TasksQueueTaskStuckIndicator extends Indicator {

    private final TasksQueue tasks;

    public TasksQueueTaskStuckIndicator(TasksQueue tasks) {
        this.tasks = tasks;
    }

    @Override
    protected IndicatorStatus doRun() {
        if (tasks.getAllStuckTasks(DateTime.now().minusMinutes(60)).isEmpty()) {
            return IndicatorStatus.green(this);
        } else {
            return IndicatorStatus.red(this);
        }
    }

    @Override
    public String getId() {
        return "tasks_queue_task_stuck";
    }

    @Override
    public String getName() {
        return "Task Stuck";
    }

    @Override
    public SystemEventType getSystemEventType() {
        return SystemEventType.HEALTH_INDICATOR_TASK_STUCK_TOGGLED;
    }

}
