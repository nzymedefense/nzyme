package app.nzyme.core.ethernet.beacons;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.security.authentication.db.TenantEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BeaconsThread extends Periodical {

    private static final Logger LOG = LogManager.getLogger(BeaconsThread.class);

    private final NzymeNode nzyme;
    private final BeaconDetector detector;

    public BeaconsThread(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.detector = new BeaconDetector(nzyme);
    }

    @Override
    protected void execute() {
        for (TenantEntry tenant : nzyme.getAuthenticationService().findAllTenantsOfAllOrganizations()) {
            try {
                detector.runForTenant(tenant.organizationUuid(), tenant.uuid());
            } catch (Exception e) {
                LOG.error("Could not run beacon detection for tenant [{}/{}]. Skipping.",
                        tenant.organizationUuid(), tenant.uuid(), e);
            }
        }
    }

    @Override
    public String getName() {
        return "BeaconsThread";
    }

}
