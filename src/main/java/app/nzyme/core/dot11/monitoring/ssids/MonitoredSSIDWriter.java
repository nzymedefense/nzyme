package app.nzyme.core.dot11.monitoring.ssids;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.SSIDWithOrganizationAndTenant;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.util.TimeRangeFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class MonitoredSSIDWriter extends Periodical {

    private static final Logger LOG = LogManager.getLogger(MonitoredSSIDWriter.class);

    private final NzymeNode nzyme;

    public MonitoredSSIDWriter(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        // Fetch all SSIDs we saw in previous minute.
        List<SSIDWithOrganizationAndTenant> ssids = nzyme.getDot11().findAllSSIDsAndOwner(TimeRangeFactory.oneMinute());

        LOG.info("SSIDS: {}", ssids);

        // findAllKnownNetworks - SSID included? touch / create
    }

    @Override
    public String getName() {
        return "MonitoredSSIDWriter";
    }

}
