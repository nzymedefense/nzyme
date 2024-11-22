package app.nzyme.core.database.tasks;

import app.nzyme.core.database.DataCategory;
import app.nzyme.plugin.distributed.tasksqueue.Task;
import app.nzyme.plugin.distributed.tasksqueue.TaskType;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public class GlobalPurgeCategoryTask extends Task {

    private final DataCategory category;

    public GlobalPurgeCategoryTask(DataCategory category) {
        this.category = category;
    }

    @Override
    public TaskType type() {
        return TaskType.PURGE_DATA_CATEGORY_GLOBAL;
    }

    @Override
    public boolean allowProcessSelf() {
        return true;
    }

    @Override
    public Map<String, Object> parameters() {
        return new HashMap<>(){{
            put("category", category);
            put("since", new DateTime().toString());
        }};
    }

    @Override
    public boolean allowRetry() {
        return true;
    }

}
