package app.nzyme.core.database.tasks.handlers;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.DataTableInformation;
import app.nzyme.plugin.distributed.tasksqueue.TaskProcessingResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class PurgeTask {

    private static final Logger LOG = LogManager.getLogger(PurgeTask.class);

    protected TaskProcessingResult purge(NzymeNode nzyme,
                                         List<UUID> tapUuids,
                                         List<DataTableInformation> tables,
                                         DateTime since) {
        if (tapUuids.isEmpty()) {
            return TaskProcessingResult.SUCCESS;
        }

        try {
            nzyme.getDatabase().useHandle(handle -> {
                for (DataTableInformation table : tables) {
                    // Check if we have a purge query. (Some tables are purged via relation/cascade)
                    if (table.getPurgeQuery() != null) {
                        handle.createUpdate(table.getPurgeQuery())
                                .bindList("taps", tapUuids)
                                .bind("since", since)
                                .execute();
                    }
                }
            });
        } catch (Exception e) {
            LOG.error("Could not purge data.", e);
        }

        return TaskProcessingResult.SUCCESS;
    }

}
