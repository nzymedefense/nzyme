package app.nzyme.core.tables.dot11;

import app.nzyme.core.rest.resources.taps.reports.tables.dot11.Dot11AdvertisedNetwork;
import app.nzyme.core.rest.resources.taps.reports.tables.dot11.Dot11BSSIDReport;
import app.nzyme.core.rest.resources.taps.reports.tables.dot11.Dot11SecurityInformationReport;
import app.nzyme.core.rest.resources.taps.reports.tables.dot11.Dot11TablesReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
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
    }

    public void writeBSSIDs(UUID tapUuid, DateTime timestamp, Map<String, Dot11BSSIDReport> bssids) {
        DateTime now = DateTime.now();

        for (Map.Entry<String, Dot11BSSIDReport> entry : bssids.entrySet()) {
            String bssid = entry.getKey();
            Dot11BSSIDReport report = entry.getValue();

            long bssidDatabaseId = tablesService.getNzyme().getDatabase().withHandle(handle ->
                    handle.createQuery("INSERT INTO dot11_bssids(uuid, tap_uuid, bssid, oui, " +
                                    "signal_strength_average, signal_strength_max, signal_strength_min, " +
                                    "hidden_ssid_frames, created_at) VALUES(:uuid, :tap_uuid, :bssid, NULL, " +
                                    ":signal_strength_average, :signal_strength_max, :signal_strength_min, " +
                                    ":hidden_ssid_frames, :created_at) RETURNING id")
                            .bind("uuid", UUID.randomUUID())
                            .bind("tap_uuid", tapUuid)
                            .bind("bssid", bssid)
                            .bind("signal_strength_average", report.signalStrength().average())
                            .bind("signal_strength_max", report.signalStrength().max())
                            .bind("signal_strength_min", report.signalStrength().min())
                            .bind("hidden_ssid_frames", report.hiddenSSIDFrames())
                            .bind("created_at", now)
                            .mapTo(Long.class)
                            .one()
            );

            // BSSID Fingerprints.
            for (String fingerprint : report.fingerprints()) {
                tablesService.getNzyme().getDatabase().useHandle(handle ->
                        handle.createUpdate("INSERT INTO dot11_fingerprints(uuid, fingerprint, bssid_id, " +
                                        "created_at) VALUES(:uuid, :fingerprint, :bssid_id, :created_at)")
                                .bind("uuid", UUID.randomUUID())
                                .bind("fingerprint", fingerprint)
                                .bind("bssid_id", bssidDatabaseId)
                                .bind("created_at", now)
                                .execute()
                );
            }

            for (Map.Entry<String, Dot11AdvertisedNetwork> ssidEntry : report.advertisedNetworks().entrySet()) {
                String ssid = ssidEntry.getKey();
                Dot11AdvertisedNetwork ssidReport = ssidEntry.getValue();

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
                        handle.createQuery("INSERT INTO dot11_ssids(uuid, bssid_id, ssid, bssid, " +
                                        "security_protocol, security_suites, is_wps, signal_strength_average, " +
                                        "signal_strength_max, signal_strength_min, created_at) VALUES(:uuid, " +
                                        ":bssid_id, :ssid, :bssid, :security_protocol, :security_suites, :is_wps, " +
                                        ":signal_strength_average, :signal_strength_max, :signal_strength_min, " +
                                        ":created_at) RETURNING *")
                                .bind("uuid", UUID.randomUUID())
                                .bind("bssid_id", bssidDatabaseId)
                                .bind("ssid", ssid)
                                .bind("bssid", bssid)
                                .bind("security_protocol", Joiner.on("/").join(securityProtocols))
                                .bind("security_suites", securitySuites)
                                .bind("is_wps", ssidReport.wps())
                                .bind("signal_strength_average", ssidReport.signalStrength().average())
                                .bind("signal_strength_max", ssidReport.signalStrength().max())
                                .bind("signal_strength_min", ssidReport.signalStrength().min())
                                .bind("created_at", now)
                                .mapTo(Long.class)
                                .one()
                );

                // SSID Fingerprints.
                for (String fingerprint : ssidReport.fingerprints()) {
                    tablesService.getNzyme().getDatabase().useHandle(handle ->
                            handle.createUpdate("INSERT INTO dot11_fingerprints(uuid, fingerprint, ssid_id, " +
                                            "created_at) VALUES(:uuid, :fingerprint, :ssid_id, :created_at)")
                                    .bind("uuid", UUID.randomUUID())
                                    .bind("fingerprint", fingerprint)
                                    .bind("ssid_id", ssidDatabaseId)
                                    .bind("created_at", now)
                                    .execute()
                    );
                }
            }
        }

    }

    @Override
    public void retentionClean() {
        // called to retention clean db tables
    }

}
