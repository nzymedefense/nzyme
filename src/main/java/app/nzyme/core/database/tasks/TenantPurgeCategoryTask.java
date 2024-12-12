package app.nzyme.core.database.tasks;

import app.nzyme.core.database.DataCategory;
import app.nzyme.plugin.distributed.tasksqueue.Task;
import app.nzyme.plugin.distributed.tasksqueue.TaskType;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TenantPurgeCategoryTask extends Task  {

    private final DataCategory category;
    private final UUID organizationId;
    private final UUID tenantid;
    private final DateTime purgeTime;

    public TenantPurgeCategoryTask(DataCategory category, UUID organizationId, UUID tenantid, DateTime purgeTime) {
        this.category = category;
        this.organizationId = organizationId;
        this.tenantid = tenantid;
        this.purgeTime = purgeTime;
    }

    @Override
    public TaskType type() {
        return TaskType.PURGE_DATA_CATEGORY_TENANT;
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
            put("tenant_id", tenantid);
            put("since", purgeTime.toString());
        }};
    }

    @Override
    public boolean allowRetry() {
        return true;
    }
}
