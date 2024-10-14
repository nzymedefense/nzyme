package app.nzyme.core.dot11.monitoring.clients;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.subsystems.Subsystem;
import app.nzyme.core.detection.alerts.DetectionType;
import app.nzyme.core.dot11.db.ConnectedClientDetails;
import app.nzyme.core.dot11.db.Dot11KnownClient;
import app.nzyme.core.dot11.db.monitoring.MonitoredBSSID;
import app.nzyme.core.dot11.db.monitoring.MonitoredSSID;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.taps.Tap;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class KnownClientMonitor extends Periodical {

    private static final Logger LOG = LogManager.getLogger(KnownClientMonitor.class);

    private final NzymeNode nzyme;

    public KnownClientMonitor(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        try {
            // Fetch all monitored networks of all tenants.
            List<MonitoredSSID> monitoredNetworks = nzyme.getDot11().findAllMonitoredSSIDs(null, null);

            nzyme.getDatabase().useHandle(handle -> {
                for (MonitoredSSID monitoredNetwork : monitoredNetworks) {
                    if (!monitoredNetwork.enabledClientMonitoring()) {
                        // Client monitoring is disabled for this network.
                        continue;
                    }

                    // Build list of taps of this tenant.
                    List<UUID> taps = nzyme.getTapManager()
                            .findAllTapsOfTenant(monitoredNetwork.organizationId(), monitoredNetwork.tenantId())
                            .stream()
                            .map(Tap::uuid)
                            .toList();

                    List<MonitoredBSSID> bssids = nzyme.getDot11()
                            .findMonitoredBSSIDsOfMonitoredNetwork(handle, monitoredNetwork.id());

                    for (MonitoredBSSID bssid : bssids) {
                        // Find all clients of BSSID.
                        List<ConnectedClientDetails> clients = nzyme.getDot11()
                                .findClientsOfBSSID(handle, bssid.bssid(), 1, taps);

                        // Process all clients.
                        for (ConnectedClientDetails client : clients) {
                            // Do we already know this client?
                            Optional<Dot11KnownClient> knownClient = nzyme.getDot11()
                                    .findKnownClient(handle, client.clientMac(), monitoredNetwork.id());

                            if (knownClient.isPresent()) {
                                // We know this client. Only update `last_seen`.
                                nzyme.getDot11().touchKnownClient(handle, knownClient.get().id());

                                // Raise alarm for new client if not approved or ignored.
                                if (!knownClient.get().isApproved() && !knownClient.get().isIgnored()) {
                                    raiseAlertIfEventingEnabled(client, monitoredNetwork);
                                }
                            } else {
                                // New client.
                                nzyme.getDot11().createKnownClient(handle, client.clientMac(), monitoredNetwork.id());

                                // Raise alarm for new client.
                                raiseAlertIfEventingEnabled(client, monitoredNetwork);
                            }
                        }
                    }
                }
            });

            // Retention clean.
            nzyme.getDot11().retentionCleanKnownClients(DateTime.now().minusDays(30));
        } catch (Exception e) {
            LOG.error("Could not run client monitoring.", e);
        }
    }

    private void raiseAlertIfEventingEnabled(ConnectedClientDetails client, MonitoredSSID ssid) {
        if  (!ssid.enabledClientEventing()) {
            LOG.debug("Not raising alert for unapproved client [{}] because eventing is enabled for " +
                    "monitored network [{}].", client.clientMac(), ssid.ssid());
            return;
        }

        Map<String, String> parameters = Maps.newHashMap();
        parameters.put("monitored_network", ssid.ssid());
        parameters.put("client_mac", client.clientMac());

        nzyme.getDetectionAlertService().raiseAlert(
                ssid.organizationId(),
                ssid.tenantId(),
                ssid.uuid(),
                null,
                DetectionType.DOT11_UNAPPROVED_CLIENT,
                Subsystem.DOT11,
                "Unapproved client \"" + client.clientMac() + "\" connected to monitored network \""  +ssid.ssid() + "\" detected.",
                parameters,
                new String[]{"monitored_network", "client_mac"},
                null
        );
    }

    @Override
    public String getName() {
        return "KnownClientMonitor";
    }

}
