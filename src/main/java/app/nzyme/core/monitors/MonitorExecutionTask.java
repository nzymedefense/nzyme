package app.nzyme.core.monitors;

import app.nzyme.core.monitors.db.MonitorEntry;
import app.nzyme.plugin.distributed.tasksqueue.Task;
import app.nzyme.plugin.distributed.tasksqueue.TaskType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MonitorExecutionTask extends Task {

    private final String serializedMonitor;
    private static final ObjectMapper OM = JsonMapper.builder()
            .addModule(new JodaModule())
            .build();

    public MonitorExecutionTask(MonitorEntry monitor) {
        this.serializedMonitor = OM.writeValueAsString(monitor);
    }

    @Override
    public TaskType type() {
        return TaskType.MONITOR_EXECUTION;
    }

    @Override
    public boolean allowProcessSelf() {
        return true;
    }

    @Override
    public Map<String, Object> parameters() {
        return new HashMap<>(){{
            put("monitor", serializedMonitor);
        }};
    }

    @Override
    public boolean allowRetry() {
        return false;
    }

}
