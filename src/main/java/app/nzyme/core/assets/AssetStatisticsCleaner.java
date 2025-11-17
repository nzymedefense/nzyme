package app.nzyme.core.assets;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.security.authentication.db.TenantEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class AssetStatisticsCleaner extends Periodical {

    private static final Logger LOG = LogManager.getLogger(AssetStatisticsCleaner.class);

    private final NzymeNode nzyme;

    public AssetStatisticsCleaner(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        for (TenantEntry tenant : nzyme.getAuthenticationService().findAllTenantsOfAllOrganizations()) {
            try {
                int retentionTimeDays = Integer.parseInt(nzyme.getDatabaseCoreRegistry().getValue(
                        AssetRegistryKeys.ASSETS_STATISTICS_RETENTION_TIME_DAYS.key(),
                        tenant.organizationUuid(),
                        tenant.uuid()
                ).orElse(AssetRegistryKeys.ASSETS_STATISTICS_RETENTION_TIME_DAYS.defaultValue().get()));

                LOG.debug("Retention cleaning asset statistics for tenant [{}/{}]: <{}> days.",
                        tenant.organizationUuid(), tenant.uuid(), retentionTimeDays);
                DateTime cutoff = DateTime.now().minusDays(retentionTimeDays);

                nzyme.getAssetsManager().retentionCleanAssetStatistics(
                        tenant.organizationUuid(), tenant.uuid(), cutoff
                );
            } catch (Exception e) {
                LOG.error("Could not retention clean asset statistics for tenant [{}/{}]. Skipping.",
                        tenant.organizationUuid(), tenant.uuid());
            }
        }
    }

    @Override
    public String getName() {
        return "AssetStatisticsCleaner";
    }

}
