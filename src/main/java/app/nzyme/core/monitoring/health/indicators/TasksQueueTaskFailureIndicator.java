package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.events.types.SystemEventType;
import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.plugin.distributed.tasksqueue.TasksQueue;
import org.joda.time.DateTime;

public class TasksQueueTaskFailureIndicator extends Indicator {

    private final TasksQueue tasks;

    public TasksQueueTaskFailureIndicator(TasksQueue tasks) {
        this.tasks = tasks;
    }

    @Override
    protected IndicatorStatus doRun() {
        if (tasks.getAllFailedTasksSince(DateTime.now().minusHours(24)).isEmpty()) {
            return IndicatorStatus.green(this);
        } else {
            return IndicatorStatus.red(this);
        }
    }

    @Override
    public String getId() {
        return "tasks_queue_task_failure";
    }

    @Override
    public String getName() {
        return "Task Failure";
    }

    @Override
    public SystemEventType getSystemEventType() {
        return SystemEventType.HEALTH_INDICATOR_TASK_FAILURE_TOGGLED;
    }

}
