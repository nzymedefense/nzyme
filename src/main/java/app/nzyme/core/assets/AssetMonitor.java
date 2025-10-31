package app.nzyme.core.assets;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.security.authentication.db.TenantEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class AssetMonitor extends Periodical {

    private static final Logger LOG = LogManager.getLogger(AssetMonitor.class);

    private final NzymeNode nzyme;


    public AssetMonitor(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        for (TenantEntry tenant : nzyme.getAuthenticationService().findAllTenantsOfAllOrganizations()) {
            try {
                LOG.debug("Processing asset statistics for tenant [{}/{}]", tenant.organizationUuid(), tenant.uuid());
                DateTime now = DateTime.now();
                long assetCount = nzyme.getAssetsManager().countActiveAssetsOfTenant(
                        tenant.organizationUuid(), tenant.uuid()
                );

                LOG.debug("Asset statistics for tenant [{}/{}]: Count: {}",
                        tenant.organizationUuid(), tenant.uuid(), assetCount);

                nzyme.getAssetsManager().writeAssetStatistics(
                        tenant.organizationUuid(), tenant.uuid(), assetCount, now
                );
            } catch (Exception e) {
                LOG.error("Could not process asset statistics for tenant [{}/{}]. Skipping.",
                        tenant.organizationUuid(), tenant.uuid());
            }
        }
    }

    @Override
    public String getName() {
        return "AssetMonitor";
    }
}
