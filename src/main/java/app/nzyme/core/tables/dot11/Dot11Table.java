package app.nzyme.core.tables.dot11;

import app.nzyme.core.detection.alerts.DetectionType;
import app.nzyme.core.detection.alerts.Subsystem;
import app.nzyme.core.dot11.monitoring.Dot11Bandits;
import app.nzyme.core.rest.resources.taps.reports.tables.dot11.*;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.taps.Tap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Dot11Table implements DataTable {

    private static final Logger LOG = LogManager.getLogger(Dot11Table.class);

    private final TablesService tablesService;
    private final ObjectMapper om;

    public Dot11Table(TablesService tablesService) {
        this.tablesService = tablesService;
        this.om = new ObjectMapper();
    }

    public void handleReport(UUID tapUuid, DateTime timestamp, Dot11TablesReport report) {
        writeBSSIDs(tapUuid, timestamp, report.bssids());
        writeClients(tapUuid, timestamp, report.clients());

        handleAlerts(tapUuid, report.alerts());
    }

    private void writeClients(UUID tapUuid, DateTime timestamp, Map<String, Dot11ClientReport> clients) {
        for (Map.Entry<String, Dot11ClientReport> entry : clients.entrySet()) {
            String clientMac = entry.getKey();
            Dot11ClientReport report = entry.getValue();

            long clientDatabaseId = tablesService.getNzyme().getDatabase().withHandle(handle ->
                    handle.createQuery("INSERT INTO dot11_clients(tap_uuid, client_mac, wildcard_probe_requests, " +
                                    "created_at) VALUES(:tap_uuid, :client_mac, :wildcard_probe_requests, " +
                                    ":created_at) RETURNING id")
                            .bind("tap_uuid", tapUuid)
                            .bind("client_mac", clientMac)
                            .bind("wildcard_probe_requests", report.wildcardProbeRequests())
                            .bind("created_at", timestamp)
                            .mapTo(Long.class)
                            .one()
            );

            for (Map.Entry<String, Long> pr : report.probeRequestSSIDs().entrySet()) {
                tablesService.getNzyme().getDatabase().withHandle(handle ->
                        handle.createUpdate("INSERT INTO dot11_client_probereq_ssids(client_id, ssid, frame_count, " +
                                        "tap_uuid) VALUES(:client_id, :ssid, :frame_count, :tap_uuid)")
                                .bind("client_id", clientDatabaseId)
                                .bind("ssid", pr.getKey())
                                .bind("frame_count", pr.getValue())
                                .bind("tap_uuid", tapUuid)
                                .execute()
                );
            }

        }

    }

    public void writeBSSIDs(UUID tapUuid, DateTime timestamp, Map<String, Dot11BSSIDReport> bssids) {
        for (Map.Entry<String, Dot11BSSIDReport> entry : bssids.entrySet()) {
            String bssid = entry.getKey();
            Dot11BSSIDReport report = entry.getValue();

            long bssidDatabaseId = tablesService.getNzyme().getDatabase().withHandle(handle ->
                    handle.createQuery("INSERT INTO dot11_bssids(tap_uuid, bssid, oui, " +
                                    "signal_strength_average, signal_strength_max, signal_strength_min, " +
                                    "hidden_ssid_frames, created_at) VALUES(:tap_uuid, :bssid, NULL, " +
                                    ":signal_strength_average, :signal_strength_max, :signal_strength_min, " +
                                    ":hidden_ssid_frames, :created_at) RETURNING id")
                            .bind("tap_uuid", tapUuid)
                            .bind("bssid", bssid)
                            .bind("signal_strength_average", report.signalStrength().average())
                            .bind("signal_strength_max", report.signalStrength().max())
                            .bind("signal_strength_min", report.signalStrength().min())
                            .bind("hidden_ssid_frames", report.hiddenSSIDFrames())
                            .bind("created_at", timestamp)
                            .mapTo(Long.class)
                            .one()
            );

            // BSSID Fingerprints.
            for (String fingerprint : report.fingerprints()) {
                tablesService.getNzyme().getDatabase().useHandle(handle ->
                        handle.createUpdate("INSERT INTO dot11_fingerprints(fingerprint, bssid_id) " +
                                        "VALUES(:fingerprint, :bssid_id)")
                                .bind("fingerprint", fingerprint)
                                .bind("bssid_id", bssidDatabaseId)
                                .execute()
                );
            }

            // BSSID Clients.
            for (Map.Entry<String, Dot11ClientStatisticsReport> client : report.clients().entrySet()) {
                String mac = client.getKey();
                Dot11ClientStatisticsReport stats = client.getValue();

                if (!bssid.equals(mac)) { // Don't record BSSID itself.
                    tablesService.getNzyme().getDatabase().useHandle(handle ->
                            handle.createUpdate("INSERT INTO dot11_bssid_clients(bssid_id, client_mac, tx_frames, " +
                                            "tx_bytes, rx_frames, rx_bytes) VALUES(:bssid_id, :client_mac, " +
                                            ":tx_frames, :tx_bytes, :rx_frames, :rx_bytes)")
                                    .bind("bssid_id", bssidDatabaseId)
                                    .bind("client_mac", mac)
                                    .bind("tx_frames", stats.txFrames())
                                    .bind("tx_bytes", stats.txBytes())
                                    .bind("rx_frames", stats.rxFrames())
                                    .bind("rx_bytes", stats.rxBytes())
                                    .execute()
                    );
                }
            }

            for (Map.Entry<String, Dot11AdvertisedNetworkReport> ssidEntry : report.advertisedNetworks().entrySet()) {
                try {
                    String ssid = ssidEntry.getKey();
                    Dot11AdvertisedNetworkReport ssidReport = ssidEntry.getValue();

                    List<String> securityProtocols = Lists.newArrayList();
                    Map<String, String> suiteMap = Maps.newHashMap();
                    for (Dot11SecurityInformationReport sec : ssidReport.security()) {
                        securityProtocols.addAll(sec.protocols());

                        suiteMap.put("group_cipher", sec.suites().groupCipher());
                        suiteMap.put("pairwise_ciphers",
                                Joiner.on(",").join(sec.suites().pairwiseCiphers()));
                        suiteMap.put("key_management_modes",
                                Joiner.on(",").join(sec.suites().keyManagementModes()));
                    }

                    String securitySuites;
                    try {
                        securitySuites = this.om.writeValueAsString(suiteMap);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    Long ssidDatabaseId = tablesService.getNzyme().getDatabase().withHandle(handle ->
                            handle.createQuery("INSERT INTO dot11_ssids(bssid_id, tap_uuid, ssid, bssid, " +
                                            "security_protocol, security_suites, is_wps, signal_strength_average, " +
                                            "signal_strength_max, signal_strength_min, beacon_advertisements, " +
                                            "proberesp_advertisements, created_at) VALUES(:bssid_id, :tap_uuid, " +
                                            ":ssid, :bssid, :security_protocol, :security_suites, :is_wps, " +
                                            ":signal_strength_average, :signal_strength_max, :signal_strength_min, " +
                                            ":beacon_advertisements, :proberesp_advertisements, :created_at) " +
                                            "RETURNING *")
                                    .bind("bssid_id", bssidDatabaseId)
                                    .bind("tap_uuid", tapUuid)
                                    .bind("ssid", ssid)
                                    .bind("bssid", bssid)
                                    .bind("security_protocol", Joiner.on("/").join(securityProtocols))
                                    .bind("security_suites", securitySuites)
                                    .bind("is_wps", ssidReport.wps())
                                    .bind("signal_strength_average", ssidReport.signalStrength().average())
                                    .bind("signal_strength_max", ssidReport.signalStrength().max())
                                    .bind("signal_strength_min", ssidReport.signalStrength().min())
                                    .bind("beacon_advertisements", ssidReport.beaconAdvertisements())
                                    .bind("proberesp_advertisements", ssidReport.probeResponseAdvertisements())
                                    .bind("created_at", timestamp)
                                    .mapTo(Long.class)
                                    .one()
                    );

                    // SSID Fingerprints.
                    for (String fingerprint : ssidReport.fingerprints()) {
                        tablesService.getNzyme().getDatabase().useHandle(handle ->
                                handle.createUpdate("INSERT INTO dot11_fingerprints(fingerprint, ssid_id) " +
                                                "VALUES(:fingerprint, :ssid_id)")
                                        .bind("fingerprint", fingerprint)
                                        .bind("ssid_id", ssidDatabaseId)
                                        .execute()
                        );
                    }

                    // SSID Rates.
                    for (Float rate : ssidReport.rates()) {
                        tablesService.getNzyme().getDatabase().useHandle(handle ->
                                handle.createUpdate("INSERT INTO dot11_rates(rate, ssid_id) " +
                                                "VALUES(:rate, :ssid_id)")
                                        .bind("rate", rate)
                                        .bind("ssid_id", ssidDatabaseId)
                                        .execute()
                        );
                    }

                    // Channel Statistics.
                    for (Map.Entry<Long, Map<String, Dot11ChannelStatisticsReport>> cs : ssidReport.channelStatistics().entrySet()) {
                        long frequency = cs.getKey();
                        for (Map.Entry<String, Dot11ChannelStatisticsReport> ft : cs.getValue().entrySet()) {
                            String frameType = ft.getKey();
                            Dot11ChannelStatisticsReport stats = ft.getValue();

                            tablesService.getNzyme().getDatabase().useHandle(handle ->
                                    handle.createUpdate("INSERT INTO dot11_channels(ssid_id, frequency, " +
                                                    "frame_type, stats_bytes, stats_frames) VALUES(:ssid_id, " +
                                                    ":frequency, :frame_type, :stats_bytes, :stats_frames)")
                                            .bind("ssid_id", ssidDatabaseId)
                                            .bind("frequency", frequency)
                                            .bind("frame_type", frameType.toLowerCase())
                                            .bind("stats_bytes", stats.bytes())
                                            .bind("stats_frames", stats.frames())
                                            .execute()
                            );
                        }
                    }

                    // Write channel signal histogram.
                    for (Map.Entry<Long, Map<Long, Long>> channel : ssidReport.signalHistogram().entrySet()) {
                        long frequency = channel.getKey();
                        for (Map.Entry<Long, Long> histo : channel.getValue().entrySet()) {
                            tablesService.getNzyme().getDatabase().useHandle(handle ->
                                    handle.createUpdate("INSERT INTO dot11_channel_histograms(ssid_id, frequency, " +
                                                    "signal_strength, frame_count) VALUES(:ssid_id, :frequency, " +
                                                    ":signal_strength, :frame_count)")
                                            .bind("ssid_id", ssidDatabaseId)
                                            .bind("frequency", frequency)
                                            .bind("signal_strength", histo.getKey())
                                            .bind("frame_count", histo.getValue())
                                            .execute()
                            );
                        }
                    }

                    // Infrastructure Types.
                    for (String infrastructureType : ssidReport.infrastructureTypes()) {
                        tablesService.getNzyme().getDatabase().useHandle(handle ->
                                handle.createUpdate("INSERT INTO dot11_infrastructure_types(infrastructure_type," +
                                                " ssid_id) VALUES(:infrastructure_type, :ssid_id)")
                                        .bind("infrastructure_type", infrastructureType.toLowerCase())
                                        .bind("ssid_id", ssidDatabaseId)
                                        .execute()
                        );
                    }
                } catch(Exception e) {
                    LOG.error("Could not write SSID.", e);
                    continue;
                }
            }
        }
    }

    private void handleAlerts(UUID tapUuid, List<Dot11AlertReport> alerts) {
        Optional<Tap> tap = tablesService.getNzyme().getTapManager().findTap(tapUuid);

        if (tap.isEmpty()) {
            LOG.error("Could not find reporting tap [{}].", tapUuid);
            return;
        }

        for (Dot11AlertReport alert : alerts) {
            switch (alert.alertType()) {
                case PwnagotchiDetected:
                    Map<String, String> attributes = reportAlertAttributesToAlertAttributes(alert.attributes());
                    attributes.put("bandit_name", Dot11Bandits.CUSTOM_PWNAGOTCHI_NAME);
                    attributes.put("bandit_description", Dot11Bandits.CUSTOM_PWNAGOTCHI_DESCRIPTION);

                    tablesService.getNzyme().getDetectionAlertService().raiseAlert(
                            tap.get().organizationId(),
                            tap.get().tenantId(),
                            null,
                            tap.get().uuid(),
                            DetectionType.DOT11_BANDIT_CONTACT,
                            Subsystem.DOT11,
                            attributes,
                            new String[]{"identity"}
                    );

                    break;
                default:
                    LOG.warn("Unknown tap alert type: [{}]. Skipping.", alert.alertType());
            }
        }

    }

    /*
     * This is somewhat over-complicated to allow Rust to populate maps with different types of values.
     * We are just calling .toString(), but that may change if we ever report more complex attributes.
     */
    private Map<String, String> reportAlertAttributesToAlertAttributes(
            Map<String, Map<Dot11AlertReport.AlertAttributeType, Object>> attributes) {
        Map<String, String> alertAttributes = Maps.newHashMap();

        for (Map.Entry<String, Map<Dot11AlertReport.AlertAttributeType, Object>> attr : attributes.entrySet()) {
            String key = attr.getKey();
            String value = null;
            for (Map.Entry<Dot11AlertReport.AlertAttributeType, Object> attrValue : attr.getValue().entrySet()) {
                switch (attrValue.getKey()) {
                    case Number:
                    case String:
                        value = attrValue.getValue().toString();
                        break;
                }
            }

            alertAttributes.put(key, value);
        }

        return alertAttributes;
    }

    @Override
    public void retentionClean() {
        // called to retention clean db tables TODO
    }

}
