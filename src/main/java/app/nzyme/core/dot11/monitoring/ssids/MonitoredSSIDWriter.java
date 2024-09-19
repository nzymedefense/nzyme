package app.nzyme.core.dot11.monitoring.ssids;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.Dot11KnownNetwork;
import app.nzyme.core.dot11.db.SSIDWithOrganizationAndTenant;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.util.TimeRangeFactory;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MonitoredSSIDWriter extends Periodical {

    private static final Logger LOG = LogManager.getLogger(MonitoredSSIDWriter.class);

    private final NzymeNode nzyme;

    public MonitoredSSIDWriter(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        try {
            // Fetch all SSIDs we saw in previous minute.
            List<SSIDWithOrganizationAndTenant> ssids = nzyme.getDot11().findAllCurrentlyActiveSSIDsAndOwner();

            Map<String, List<Dot11KnownNetwork>> knownNetworksCache = Maps.newHashMap();
            nzyme.getDatabase().useHandle(handle -> {
                for (SSIDWithOrganizationAndTenant ssid : ssids) {
                    // Don't run if SSID monitoring is disabled for this tenant.
                    Optional<String> isEnabled = nzyme.getDatabaseCoreRegistry().getValue(
                            MonitoredSSIDRegistryKeys.IS_ENABLED.key(), ssid.organizationId(), ssid.tenantId()
                    );

                    if (isEnabled.isEmpty() || !isEnabled.get().equals("true")) {
                        LOG.debug("Skipping SSID [{}] of org/tenant ({}/{}) that has monitoring disabled.",
                                ssid.ssid(), ssid.organizationId(), ssid.tenantId());
                        continue;
                    }

                    LOG.debug("Processing SSID [{}] of org/tenant ({}/{}) for monitoring.",
                            ssid, ssid.organizationId(), ssid.tenantId());

                    String key = ssid.organizationId().toString() + ssid.tenantId().toString();
                    List<Dot11KnownNetwork> knownNetworks = knownNetworksCache.get(key);
                    if (knownNetworks == null) {
                        knownNetworks = nzyme.getDot11()
                                .findAllKnownNetworks(handle, ssid.organizationId(), ssid.tenantId(), Integer.MAX_VALUE, 0);
                        knownNetworksCache.put(key, knownNetworks);
                    }

                    boolean known = false;
                    for (Dot11KnownNetwork knownNetwork : knownNetworks) {
                        if (knownNetwork.ssid().equals(ssid.ssid())) {
                            // We already have this known network. Update `last_seen`.
                            LOG.debug("Updating existing known network [{}]", knownNetwork);
                            nzyme.getDot11().touchKnownNetwork(handle, knownNetwork.id());
                            known = true;
                        }
                    }

                    if (!known) {
                        LOG.debug("Inserting new known network [{}]", ssid);
                        nzyme.getDot11().createKnownNetwork(
                                handle, ssid.ssid(), false, ssid.organizationId(), ssid.tenantId()
                        );
                    }
                }
            });

            // Retention clean.
            nzyme.getDot11().retentionCleanKnownNetworks(DateTime.now().minusDays(30));
        } catch (Exception e) {
            LOG.error("Could not run SSID monitoring.", e);
        }
    }

    @Override
    public String getName() {
        return "MonitoredSSIDWriter";
    }

}
