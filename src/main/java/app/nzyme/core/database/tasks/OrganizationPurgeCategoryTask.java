package app.nzyme.core.database.tasks;

import app.nzyme.core.database.DataCategory;
import app.nzyme.plugin.distributed.tasksqueue.Task;
import app.nzyme.plugin.distributed.tasksqueue.TaskType;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrganizationPurgeCategoryTask extends Task  {

    private final DataCategory category;
    private final UUID organizationId;

    public OrganizationPurgeCategoryTask(DataCategory category, UUID organizationId) {
        this.category = category;
        this.organizationId = organizationId;
    }

    @Override
    public TaskType type() {
        return TaskType.PURGE_DATA_CATEGORY_ORGANIZATION;
    }

    @Override
    public boolean allowProcessSelf() {
        return true;
    }

    @Override
    public Map<String, Object> parameters() {
        return new HashMap<>(){{
            put("category", category);
            put("organization_id", organizationId);
            put("since", new DateTime().toString());
        }};
    }

    @Override
    public boolean allowRetry() {
        return true;
    }
}
