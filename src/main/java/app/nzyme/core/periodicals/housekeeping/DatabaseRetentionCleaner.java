package app.nzyme.core.periodicals.housekeeping;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.DataCategory;
import app.nzyme.core.database.DatabaseTools;
import app.nzyme.core.database.tasks.TenantPurgeCategoryTask;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class DatabaseRetentionCleaner extends Periodical {

    private static final Logger LOG = LogManager.getLogger(DatabaseRetentionCleaner.class);

    private final NzymeNode nzyme;

    public DatabaseRetentionCleaner(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        LOG.debug("Starting database retention cleaning.");

        for (OrganizationEntry org : nzyme.getAuthenticationService().findAllOrganizations()) {
            for (TenantEntry tenant : nzyme.getAuthenticationService().findAllTenantsOfOrganization(org.uuid())) {
                for (DataCategory category : DataCategory.values()) {
                    DateTime since = DateTime.now().minusDays(
                            DatabaseTools.getDataCategoryRetentionTimeDays(nzyme, category, org.uuid(), tenant.uuid())
                    );

                    LOG.debug("Retention cleaning category [{}] of tenant [{}/{}]. Deleting all data older " +
                            "than [{}].", category, tenant.name(), tenant.uuid(), since);

                    nzyme.getTasksQueue().publish(
                            new TenantPurgeCategoryTask(category, org.uuid(), tenant.uuid(), since)
                    );
                }

            }

        }

    }

    @Override
    public String getName() {
        return "DatabaseRetentionCleaner";
    }

}
