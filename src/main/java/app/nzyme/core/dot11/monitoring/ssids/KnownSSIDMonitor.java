package app.nzyme.core.dot11.monitoring.ssids;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.Subsystem;
import app.nzyme.core.detection.alerts.DetectionType;
import app.nzyme.core.dot11.db.Dot11KnownNetwork;
import app.nzyme.core.dot11.db.SSIDWithOrganizationAndTenant;
import app.nzyme.core.periodicals.Periodical;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KnownSSIDMonitor extends Periodical {

    private static final Logger LOG = LogManager.getLogger(KnownSSIDMonitor.class);

    private final NzymeNode nzyme;

    public KnownSSIDMonitor(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        try {
            // Fetch all SSIDs we saw in previous minute.
            List<SSIDWithOrganizationAndTenant> ssids = nzyme.getDot11().findAllCurrentlyActiveSSIDsAndOwner(5);

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

                            // Raise alarm if network is not approved.
                            if (!knownNetwork.isApproved() && !knownNetwork.isIgnored()) {
                                raiseAlertIfEventingEnabled(ssid);
                            }
                        }
                    }

                    if (!known) {
                        LOG.debug("Inserting new known network [{}]", ssid);
                        nzyme.getDot11().createKnownNetwork(
                                handle, ssid.ssid(), false, ssid.organizationId(), ssid.tenantId()
                        );

                        // Raise alarm for unapproved network.
                        raiseAlertIfEventingEnabled(ssid);
                    }
                }
            });

            // Retention clean.
            nzyme.getDot11().retentionCleanKnownNetworks(DateTime.now().minusDays(30));
        } catch (Exception e) {
            LOG.error("Could not run SSID monitoring.", e);
        }
    }

    private void raiseAlertIfEventingEnabled(SSIDWithOrganizationAndTenant ssid) {
        Optional<String> enabled = nzyme.getDatabaseCoreRegistry().getValue(
                MonitoredSSIDRegistryKeys.EVENTING_IS_ENABLED.key(), ssid.organizationId(), ssid.tenantId()
        );

        if (enabled.isEmpty() || enabled.get().equals("false")) {
            LOG.debug("Not raising alert for unapproved SSID [{}] because eventing is enabled.", ssid.ssid());
            return;
        }

        Map<String, String> parameters = Maps.newHashMap();
        parameters.put("ssid", ssid.ssid());

        nzyme.getDetectionAlertService().raiseAlert(
                ssid.organizationId(),
                ssid.tenantId(),
                null,
                null,
                DetectionType.DOT11_UNAPPROVED_SSID,
                Subsystem.DOT11,
                "Unapproved SSID \"" + ssid.ssid() + "\" detected.",
                parameters,
                new String[]{"ssid"},
                null
        );
    }

    @Override
    public String getName() {
        return "KnownSSIDMonitor";
    }

}
