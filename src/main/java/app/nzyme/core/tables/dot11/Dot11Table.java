package app.nzyme.core.tables.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.detection.alerts.DetectionType;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.Dot11RegistryKeys;
import app.nzyme.core.dot11.db.monitoring.*;
import app.nzyme.core.dot11.bandits.Dot11BanditDescription;
import app.nzyme.core.dot11.bandits.Dot11Bandits;
import app.nzyme.core.dot11.db.monitoring.probereq.MonitoredProbeRequestEntry;
import app.nzyme.core.rest.resources.taps.reports.tables.dot11.*;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.tables.dot11.monitoring.PreLoadedMonitoredBSSID;
import app.nzyme.core.tables.dot11.monitoring.PreLoadedMonitoredSSID;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.util.MetricNames;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.Subsystem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import info.debatty.java.stringsimilarity.JaroWinkler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.joda.time.DateTime;
import com.codahale.metrics.Timer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class Dot11Table implements DataTable {

    private static final Logger LOG = LogManager.getLogger(Dot11Table.class);

    private final TablesService tablesService;
    private final ObjectMapper om;

    private final Timer totalReportTimer;
    private final Timer bssidReportTimer;
    private final Timer clientsReportTimer;
    private final Timer discoReportTimer;
    private final Timer alertTimer;

    public Dot11Table(TablesService tablesService) {
        this.tablesService = tablesService;
        this.om = new ObjectMapper();

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DOT11_TOTAL_REPORT_PROCESSING_TIMER);
        this.bssidReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DOT11_BSSID_REPORT_PROCESSING_TIMER);
        this.clientsReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DOT11_CLIENTS_REPORT_PROCESSING_TIMER);
        this.discoReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DOT11_DISCO_REPORT_PROCESSING_TIMER);
        this.alertTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DOT11_ALERT_PROCESSING_TIMER);
    }

    public void handleReport(UUID tapUuid, DateTime timestamp, Dot11TablesReport report) {
        try (Timer.Context ignored = totalReportTimer.time()) {
            Optional<Tap> tap = tablesService.getNzyme().getTapManager().findTap(tapUuid);

            if (tap.isEmpty()) {
                LOG.warn("Not handling report of unknown tap [{}].", tapUuid);
                return;
            }

            try (Timer.Context ignored2 = bssidReportTimer.time()) {
                writeBSSIDs(tap.get(), timestamp, report.bssids(), tap.get().organizationId(), tap.get().tenantId());
            }

            try (Timer.Context ignored2 = clientsReportTimer.time()) {
                writeClients(tap.get(), timestamp, report.clients());
            }

            try (Timer.Context ignored2 = discoReportTimer.time()) {
                writeDisco(tap.get(), timestamp, report.disco());
            }

            try (Timer.Context ignored2 = alertTimer.time()) {
                handleAlerts(tap.get(), report.alerts());
            }
        }
    }

    private void writeClients(Tap tap, DateTime timestamp, Map<String, Dot11ClientReport> clients) {
        Map<String, MonitoredProbeRequestEntry> monitoredProbeRequests = tablesService.getNzyme().getDot11()
                .findAllMonitoredProbeRequests(tap.organizationId(), tap.tenantId(), Integer.MAX_VALUE, 0)
                .stream()
                .collect(Collectors.toMap(MonitoredProbeRequestEntry::ssid, entry -> entry));

        for (Map.Entry<String, Dot11ClientReport> entry : clients.entrySet()) {
            String clientMac = entry.getKey();
            Dot11ClientReport report = entry.getValue();

            long clientDatabaseId = tablesService.getNzyme().getDatabase().withHandle(handle ->
                    handle.createQuery("INSERT INTO dot11_clients(tap_uuid, client_mac, client_mac_is_randomized, " +
                                    "wildcard_probe_requests, signal_strength_average, signal_strength_max, " +
                                    "signal_strength_min, created_at) VALUES(:tap_uuid, :client_mac, " +
                                    ":client_mac_is_randomized, :wildcard_probe_requests, :signal_strength_average, " +
                                    ":signal_strength_max, :signal_strength_min, :created_at) RETURNING id")
                            .bind("tap_uuid", tap.uuid())
                            .bind("client_mac", clientMac)
                            .bind("client_mac_is_randomized", Tools.macAddressIsRandomized(clientMac))
                            .bind("wildcard_probe_requests", report.wildcardProbeRequests())
                            .bind("signal_strength_average", report.signalStrength().average())
                            .bind("signal_strength_min", report.signalStrength().min())
                            .bind("signal_strength_max", report.signalStrength().max())
                            .bind("created_at", timestamp)
                            .mapTo(Long.class)
                            .one()
            );

            tablesService.getNzyme().getDatabase().useHandle(handle -> {
                PreparedBatch batch = handle.prepareBatch(
                        "INSERT INTO dot11_client_probereq_ssids(client_id, ssid, frame_count, tap_uuid) " +
                                "VALUES(:client_id, :ssid, :frame_count, :tap_uuid)"
                );

                for (Map.Entry<String, Long> pr : report.probeRequestSSIDs().entrySet()) {
                    String ssid = Tools.sanitizeSSID(pr.getKey());

                    // Check if we are monitoring for this probe request SSID and raise alert if so.
                    if (monitoredProbeRequests.containsKey(ssid)) {
                        // Raise alert.
                        Map<String, String> attributes = Maps.newHashMap();
                        attributes.put("ssid", ssid);
                        attributes.put("client_mac", clientMac);

                        tablesService.getNzyme().getDetectionAlertService().raiseAlert(
                                tap.organizationId(),
                                tap.tenantId(),
                                null,
                                tap.uuid(),
                                DetectionType.DOT11_PROBEREQ,
                                Subsystem.DOT11,
                                "Monitored probe request for SSID \"" + ssid + "\" detected in range.",
                                attributes,
                                new String[]{"ssid"},
                                report.signalStrength().average()
                        );
                    }

                    batch.bind("client_id", clientDatabaseId)
                            .bind("ssid", ssid)
                            .bind("frame_count", pr.getValue())
                            .bind("tap_uuid", tap.uuid())
                            .add();
                }

                batch.execute();
            });
        }
    }

    public void writeBSSIDs(Tap tap, DateTime timestamp,
                            Map<String, Dot11BSSIDReport> bssids,
                            UUID organizationId,
                            UUID tenantId) {
        // Collect all monitored SSIDs and their attributes.
        Map<String, PreLoadedMonitoredSSID> monitoredSSIDs = Maps.newHashMap();
        List<String> monitoredSSIDNames = Lists.newArrayList();
        NzymeNode nzyme = tablesService.getNzyme();

        nzyme.getDatabase().useHandle(handle -> {
            for (MonitoredSSID s : nzyme.getDot11().findAllMonitoredSSIDs(tap.organizationId(), tap.tenantId())) {
                if (!s.isEnabled()) {
                    continue;
                }

                monitoredSSIDNames.add(s.ssid());

                Map<String, PreLoadedMonitoredBSSID> preLoadedBSSIDs = Maps.newHashMap();
                for (MonitoredBSSID b : nzyme.getDot11().findMonitoredBSSIDsOfMonitoredNetwork(s.id())) {
                    List<String> fingerprints = Lists.newArrayList();
                    for (MonitoredFingerprint f : nzyme.getDot11().findMonitoredFingerprintsOfMonitoredBSSID(b.id())) {
                        fingerprints.add(f.fingerprint());
                    }

                    preLoadedBSSIDs.put(b.bssid(), PreLoadedMonitoredBSSID.create(b.bssid(), fingerprints));
                }

                List<Integer> preLoadedChannels = Lists.newArrayList();
                for (MonitoredChannel c : nzyme.getDot11().findMonitoredChannelsOfMonitoredNetwork(s.id())) {
                    preLoadedChannels.add((int) c.frequency());
                }

                List<String> preLoadedSecuritySuites = Lists.newArrayList();
                for (MonitoredSecuritySuite ss : nzyme.getDot11().findMonitoredSecuritySuitesOfMonitoredNetwork(s.id())) {
                    preLoadedSecuritySuites.add(ss.securitySuite());
                }

                monitoredSSIDs.put(s.ssid(), PreLoadedMonitoredSSID.create(
                        s.id(),
                        s.uuid(),
                        s.ssid(),
                        preLoadedBSSIDs,
                        preLoadedChannels,
                        preLoadedSecuritySuites,
                        s.enabledUnexpectedBSSID(),
                        s.enabledUnexpectedChannel(),
                        s.enabledUnexpectedSecuritySuites(),
                        s.enabledUnexpectedFingerprint(),
                        s.enabledUnexpectedSignalTracks(),
                        s.enabledSimilarLookingSSID(),
                        s.enabledSSIDSubstring(),
                        s.detectionConfigSimilarLookingSSIDThreshold()
                ));
            }


            // Load all bandits.
            List<Dot11BanditDescription> bandits = Lists.newArrayList(Dot11Bandits.BUILT_IN);
            for (CustomBanditDescription bandit : nzyme.getDot11()
                    .findAllCustomBandits(organizationId, tenantId, Integer.MAX_VALUE, 0)) {
                List<String> fingerprints = nzyme.getDot11().findFingerprintsOfCustomBandit(bandit.id());

                bandits.add(Dot11BanditDescription.create(
                        bandit.uuid().toString(),
                        true,
                        bandit.name(),
                        bandit.description(),
                        fingerprints
                ));
            }

            List<SSIDProcessingTask> ssidProcessingTasks = Lists.newArrayList();

            for (Map.Entry<String, Dot11BSSIDReport> entry : bssids.entrySet()) {
                String bssid = entry.getKey();
                Dot11BSSIDReport report = entry.getValue();

                long bssidDatabaseId = handle.createQuery(
                        "INSERT INTO dot11_bssids(tap_uuid, bssid, oui, " +
                                "signal_strength_average, signal_strength_max, signal_strength_min, " +
                                "hidden_ssid_frames, created_at) VALUES(:tap_uuid, :bssid, NULL, " +
                                ":signal_strength_average, :signal_strength_max, :signal_strength_min, " +
                                ":hidden_ssid_frames, :created_at) RETURNING id")
                        .bind("tap_uuid", tap.uuid())
                        .bind("bssid", bssid)
                        .bind("signal_strength_average", report.signalStrength().average())
                        .bind("signal_strength_max", report.signalStrength().max())
                        .bind("signal_strength_min", report.signalStrength().min())
                        .bind("hidden_ssid_frames", report.hiddenSSIDFrames())
                        .bind("created_at", timestamp)
                        .mapTo(Long.class)
                        .one();

                // BSSID Fingerprints.
                PreparedBatch fingerprintBatch = handle.prepareBatch(
                        "INSERT INTO dot11_fingerprints(fingerprint, bssid_id) " +
                                "VALUES(:fingerprint, :bssid_id)");
                for (String fingerprint : report.fingerprints()) {
                    fingerprintBatch
                            .bind("fingerprint", fingerprint)
                            .bind("bssid_id", bssidDatabaseId)
                            .add();

                    // Is this a known bandit fingerprint?
                    for (Dot11BanditDescription bandit : bandits) {
                        if (bandit.fingerprints() != null && bandit.fingerprints().contains(fingerprint)) {
                            Map<String, String> attributes = Maps.newHashMap();
                            attributes.put("fingerprint", fingerprint);
                            attributes.put("bssid", bssid);
                            attributes.put("tap_uuid", tap.uuid().toString());
                            attributes.put("bandit_name", bandit.name());
                            attributes.put("bandit_description", bandit.description());
                            attributes.put("bandit_is_custom", String.valueOf(bandit.isCustom()));

                            tablesService.getNzyme().getDetectionAlertService().raiseAlert(
                                    tap.organizationId(),
                                    tap.tenantId(),
                                    null,
                                    tap.uuid(),
                                    DetectionType.DOT11_BANDIT_CONTACT,
                                    Subsystem.DOT11,
                                    "Bandit \"" + bandit.name() + "\" advertising BSSID \"" + bssid + "\" " +
                                            "detected in range.",
                                    attributes,
                                    new String[]{"bssid", "fingerprint", "bandit_is_custom"},
                                    report.signalStrength().average()
                            );
                        }
                    }
                }
                fingerprintBatch.execute();

                // BSSID Clients.
                PreparedBatch bssidClientsBatch = handle.prepareBatch(
                        "INSERT INTO dot11_bssid_clients(bssid_id, client_mac, tx_frames, " +
                                "tx_bytes, rx_frames, rx_bytes, signal_strength_average, " +
                                "signal_strength_min, signal_strength_max) VALUES(:bssid_id, :client_mac, " +
                                ":tx_frames, :tx_bytes, :rx_frames, :rx_bytes, :signal_strength_average, " +
                                ":signal_strength_min, :signal_strength_max)");

                for (Map.Entry<String, Dot11ClientStatisticsReport> client : report.clients().entrySet()) {
                    String mac = client.getKey();
                    Dot11ClientStatisticsReport stats = client.getValue();

                    if (!bssid.equals(mac)) { // Don't record BSSID itself.
                        bssidClientsBatch
                                .bind("bssid_id", bssidDatabaseId)
                                .bind("client_mac", mac)
                                .bind("tx_frames", stats.txFrames())
                                .bind("tx_bytes", stats.txBytes())
                                .bind("rx_frames", stats.rxFrames())
                                .bind("rx_bytes", stats.rxBytes())
                                .bind("signal_strength_average", stats.signalStrength().average())
                                .bind("signal_strength_min", stats.signalStrength().average())
                                .bind("signal_strength_max", stats.signalStrength().average())
                                .add();
                    }
                }
                bssidClientsBatch.execute();

                // Pre-process all SSIDs.
                for (Map.Entry<String, Dot11AdvertisedNetworkReport> ssidEntry : report.advertisedNetworks().entrySet()) {
                    ssidProcessingTasks.add(SSIDProcessingTask.create(
                            bssid, ssidEntry.getKey(), ssidEntry.getValue(), bssidDatabaseId, tap, timestamp
                    ));
                }
            }

            // Write all SSIDs.
            CountDownLatch latch = new CountDownLatch(ssidProcessingTasks.size());
            for (SSIDProcessingTask ssidProcessingTask : ssidProcessingTasks) {
                tablesService.getProcessorPool().submit(() -> {
                    writeSSID(nzyme, handle, monitoredSSIDNames, monitoredSSIDs, ssidProcessingTask);

                    latch.countDown();
                });
            }

            // Wait for SSID processing to finish.
            try {
                latch.await();
            } catch (InterruptedException e) {
                LOG.error("SSID writer process interrupted.", e);
            }
        });
    }

    private void writeSSID(NzymeNode nzyme,
                           Handle handle,
                           List<String> monitoredSSIDNames,
                           Map<String, PreLoadedMonitoredSSID> monitoredSSIDs,
                           SSIDProcessingTask task) {
        try {
            // Replace all non-printable characters.
            final String ssid = Tools.sanitizeSSID(task.ssid());

            /*
             * If all characters were sanitized away, this is a hidden SSID.
             * (some access points build hidden SSIDs this way)
             */
            if (ssid.isEmpty()) {
                return;
            }

            Long ssidDatabaseId = handle.createQuery(
                    "INSERT INTO dot11_ssids(bssid_id, tap_uuid, ssid, bssid, " +
                            "signal_strength_average, signal_strength_max, signal_strength_min, " +
                            "beacon_advertisements, proberesp_advertisements, created_at) " +
                            "VALUES(:bssid_id, :tap_uuid, :ssid, :bssid, :signal_strength_average, " +
                            ":signal_strength_max, :signal_strength_min, :beacon_advertisements, " +
                            ":proberesp_advertisements, :created_at) RETURNING *")
                    .bind("bssid_id", task.bssidDatabaseId())
                    .bind("tap_uuid", task.tap().uuid())
                    .bind("ssid", ssid)
                    .bind("bssid", task.bssid())
                    .bind("signal_strength_average", task.ssidReport().signalStrength().average())
                    .bind("signal_strength_max", task.ssidReport().signalStrength().max())
                    .bind("signal_strength_min", task.ssidReport().signalStrength().min())
                    .bind("beacon_advertisements", task.ssidReport().beaconAdvertisements())
                    .bind("proberesp_advertisements", task.ssidReport().probeResponseAdvertisements())
                    .bind("created_at", task.timestamp())
                    .mapTo(Long.class)
                    .one();

            // WPS settings.
            PreparedBatch wpsBatch = handle.prepareBatch(
                    "INSERT INTO dot11_ssid_settings(ssid_id, attribute, value) " +
                            "VALUES(:ssid_id, 'has_wps', :value)");
            for (boolean hasWps : task.ssidReport().wps()) {
                wpsBatch.bind("ssid_id", ssidDatabaseId).bind("value", String.valueOf(hasWps)).add();
            }
            wpsBatch.execute();

            // Security protocols and suites.
            PreparedBatch noneSettingsBatch = handle.prepareBatch(
                    "INSERT INTO dot11_ssid_settings(ssid_id, attribute, value) " +
                            "VALUES(:ssid_id, 'security_protocol', NULL"); // We insert NULL to signal "NONE".
            PreparedBatch someSettingsBatch = handle.prepareBatch(
                    "INSERT INTO dot11_ssid_settings(ssid_id, attribute, value) " +
                            "VALUES(:ssid_id, 'security_protocol', :value)");
            PreparedBatch suitesBatch = handle.prepareBatch(
                    "INSERT INTO dot11_ssid_settings(ssid_id, attribute, value) " +
                            "VALUES(:ssid_id, 'security_suite', :value)");
            for (Dot11SecurityInformationReport sec : task.ssidReport().security()) {
                if (sec.protocols().isEmpty()) {
                    noneSettingsBatch.bind("ssid_id", ssidDatabaseId).add();
                } else {
                    for (String protocol : sec.protocols()) {
                        someSettingsBatch
                                .bind("ssid_id", ssidDatabaseId)
                                .bind("value", protocol)
                                .add();
                    }
                }

                Map<String, String> suiteMap = Maps.newHashMap();
                suiteMap.put("group_cipher", sec.suites().groupCipher());
                suiteMap.put("pairwise_ciphers",
                        Joiner.on(",").join(sec.suites().pairwiseCiphers()));
                suiteMap.put("key_management_modes",
                        Joiner.on(",").join(sec.suites().keyManagementModes()));
                suiteMap.put("pmf_mode", sec.pmf());

                try {
                    suitesBatch
                            .bind("ssid_id", ssidDatabaseId)
                            .bind("value", this.om.writeValueAsString(suiteMap))
                            .add();
                } catch(JsonProcessingException e) {
                    LOG.error("Could not serialize SSID <{}> security suites.", task.bssidDatabaseId(), e);
                }
            }

            noneSettingsBatch.execute();
            someSettingsBatch.execute();
            suitesBatch.execute();

            // SSID Fingerprints.
            PreparedBatch fingerprintsBatch = handle.prepareBatch(
                    "INSERT INTO dot11_fingerprints(fingerprint, ssid_id) " +
                            "VALUES(:fingerprint, :ssid_id)");
            for (String fingerprint : task.ssidReport().fingerprints()) {
                    fingerprintsBatch.bind("fingerprint", fingerprint)
                            .bind("ssid_id", ssidDatabaseId)
                            .add();
            }
            fingerprintsBatch.execute();

            // SSID Rates.
            PreparedBatch ratesBatch = handle.prepareBatch("INSERT INTO dot11_rates(rate, ssid_id) " +
                    "VALUES(:rate, :ssid_id)");
            for (Float rate : task.ssidReport().rates()) {
                ratesBatch.bind("rate", rate).bind("ssid_id", ssidDatabaseId).add();
            }
            ratesBatch.execute();

            // Channel Statistics.
            PreparedBatch statsBatch = handle.prepareBatch(
                    "INSERT INTO dot11_channels(ssid_id, frequency, " +
                            "frame_type, stats_bytes, stats_frames) VALUES(:ssid_id, " +
                            ":frequency, :frame_type, :stats_bytes, :stats_frames)");
            for (Map.Entry<Long, Map<String, Dot11ChannelStatisticsReport>> cs : task.ssidReport().channelStatistics().entrySet()) {
                long frequency = cs.getKey();
                for (Map.Entry<String, Dot11ChannelStatisticsReport> ft : cs.getValue().entrySet()) {
                    String frameType = ft.getKey();
                    Dot11ChannelStatisticsReport stats = ft.getValue();

                    statsBatch
                            .bind("ssid_id", ssidDatabaseId)
                            .bind("frequency", frequency)
                            .bind("frame_type", frameType.toLowerCase())
                            .bind("stats_bytes", stats.bytes())
                            .bind("stats_frames", stats.frames())
                            .add();
                }
            }
            statsBatch.execute();

            // Write channel signal histogram.
            PreparedBatch histoBatch = handle.prepareBatch(
                    "INSERT INTO dot11_channel_histograms(ssid_id, frequency, " +
                            "signal_strength, frame_count) VALUES(:ssid_id, :frequency, " +
                            ":signal_strength, :frame_count)");
            for (Map.Entry<Long, Map<Long, Long>> channel : task.ssidReport().signalHistogram().entrySet()) {
                long frequency = channel.getKey();
                for (Map.Entry<Long, Long> histo : channel.getValue().entrySet()) {
                    histoBatch
                            .bind("ssid_id", ssidDatabaseId)
                            .bind("frequency", frequency)
                            .bind("signal_strength", histo.getKey())
                            .bind("frame_count", histo.getValue())
                            .add();
                }
            }
            histoBatch.execute();

            // Infrastructure Types.
            PreparedBatch infraBatch = handle.prepareBatch(
                    "INSERT INTO dot11_infrastructure_types(infrastructure_type, " +
                            "ssid_id) VALUES(:infrastructure_type, :ssid_id)");
            for (String infrastructureType : task.ssidReport().infrastructureTypes()) {
                infraBatch
                        .bind("infrastructure_type", infrastructureType.toLowerCase())
                        .bind("ssid_id", ssidDatabaseId)
                        .add();
            }
            infraBatch.execute();

            /*
             * Check if this SSID is similar to any monitored SSIDs or includes a monitored substring. Skip
             * other monitored SSIDs because they are considered trusted.
             */
            JaroWinkler jaroWinkler = new JaroWinkler();
            for (PreLoadedMonitoredSSID monitoredSSID : monitoredSSIDs.values()) {
                if (!monitoredSSIDNames.contains(ssid)) {
                    // Similar looking SSIDs.
                    if (monitoredSSID.enabledSimilarLookingSSID()) {
                        double similarity = jaroWinkler
                                .similarity(monitoredSSID.ssid().toLowerCase(), ssid.toLowerCase()) * 100.0;

                        if (similarity > monitoredSSID.detectionConfigSimilarLookingSSIDThreshold()) {
                            Map<String, String> attributes = Maps.newHashMap();
                            attributes.put("similar_ssid", ssid);
                            attributes.put("similarity", String.valueOf(similarity));
                            attributes.put("similarity_threshold",
                                    String.valueOf(monitoredSSID.detectionConfigSimilarLookingSSIDThreshold()));

                            nzyme.getDetectionAlertService().raiseAlert(
                                    task.tap().organizationId(),
                                    task.tap().tenantId(),
                                    monitoredSSID.uuid(),
                                    task.tap().uuid(),
                                    DetectionType.DOT11_MONITOR_SIMILAR_LOOKING_SSID,
                                    Subsystem.DOT11,
                                    "SSID \"" + ssid + "\" looking similar to monitored network SSID " +
                                            "\"" + monitoredSSID.ssid() + "\"",
                                    attributes,
                                    new String[]{"similar_ssid"},
                                    task.ssidReport().signalStrength().average()
                            );
                        }
                    }

                    // Restricted substrings.
                    if (monitoredSSID.enabledSSIDSubstring()) {
                        // Pull all restricted substrings.
                        for (RestrictedSSIDSubstring rss :
                                nzyme.getDot11().findAllRestrictedSSIDSubstrings(monitoredSSID.id())) {
                            if (ssid.toLowerCase().contains(rss.substring().toLowerCase())) {
                                Map<String, String> attributes = Maps.newHashMap();
                                attributes.put("ssid", ssid);
                                attributes.put("restricted_substring", rss.substring());

                                nzyme.getDetectionAlertService().raiseAlert(
                                        task.tap().organizationId(),
                                        task.tap().tenantId(),
                                        monitoredSSID.uuid(),
                                        task.tap().uuid(),
                                        DetectionType.DOT11_MONITOR_SSID_SUBSTRING,
                                        Subsystem.DOT11,
                                        "SSID \"" + ssid + "\" contains restricted " +
                                                "substring \"" + rss.substring() + "\"",
                                        attributes,
                                        new String[]{"ssid", "restricted_substring"},
                                        task.ssidReport().signalStrength().average()
                                );
                            }
                        }
                    }
                }
            }

            // Network Monitoring / Alerting.
            PreLoadedMonitoredSSID monitoredSSID = monitoredSSIDs.get(ssid);
            if (monitoredSSID != null) {
                // This is a monitored SSID.

                PreLoadedMonitoredBSSID monitoredBSSID = monitoredSSID.bssids().get(task.bssid());
                if (monitoredBSSID == null) {
                    if (monitoredSSID.enabledUnexpectedBSSID()) {
                        // Unexpected BSSID.
                        Map<String, String> attributes = Maps.newHashMap();
                        attributes.put("bssid", task.bssid());

                        nzyme.getDetectionAlertService().raiseAlert(
                                task.tap().organizationId(),
                                task.tap().tenantId(),
                                monitoredSSID.uuid(),
                                task.tap().uuid(),
                                DetectionType.DOT11_MONITOR_BSSID,
                                Subsystem.DOT11,
                                "Monitored network \"" + monitoredSSID.ssid() + "\" advertised with " +
                                        "unexpected BSSID \"" + task.bssid() + "\"",
                                attributes,
                                new String[]{"bssid"},
                                task.ssidReport().signalStrength().average()
                        );
                    }
                } else {
                    // Expected BSSID. Compare fingerprints.
                    if (monitoredSSID.enabledUnexpectedFingerprint()) {
                        for (String observedFingerprint : task.ssidReport().fingerprints()) {
                            if (!monitoredBSSID.fingerprints().contains(observedFingerprint)) {
                                // Unexpected fingerprint.
                                Map<String, String> attributes = Maps.newHashMap();
                                attributes.put("bssid", task.bssid());
                                attributes.put("fingerprint", observedFingerprint);

                                nzyme.getDetectionAlertService().raiseAlert(
                                        task.tap().organizationId(),
                                        task.tap().tenantId(),
                                        monitoredSSID.uuid(),
                                        task.tap().uuid(),
                                        DetectionType.DOT11_MONITOR_FINGERPRINT,
                                        Subsystem.DOT11,
                                        "Monitored network \"" + monitoredSSID.ssid() + "\" advertised " +
                                                "with unexpected fingerprint \"" + observedFingerprint + "\".",
                                        attributes,
                                        new String[]{"bssid", "fingerprint"},
                                        task.ssidReport().signalStrength().average()
                                );
                            }
                        }
                    }
                }

                if (monitoredSSID.enabledUnexpectedChannel()) {
                    for (Long frequency : task.ssidReport().channelStatistics().keySet()) {
                        if (!monitoredSSID.channels().contains(frequency.intValue())) {
                            // Unexpected channel.
                            Map<String, String> attributes = Maps.newHashMap();
                            attributes.put("frequency", String.valueOf(frequency));

                            nzyme.getDetectionAlertService().raiseAlert(
                                    task.tap().organizationId(),
                                    task.tap().tenantId(),
                                    monitoredSSID.uuid(),
                                    task.tap().uuid(),
                                    DetectionType.DOT11_MONITOR_CHANNEL,
                                    Subsystem.DOT11,
                                    "Monitored network \"" + monitoredSSID.ssid() + "\" advertised on " +
                                            "unexpected frequency " + frequency + "MHz",
                                    attributes,
                                    new String[]{"frequency"},
                                    task.ssidReport().signalStrength().average()
                            );
                        }
                    }
                }

                if (monitoredSSID.enabledUnexpectedSecuritySuites()) {
                    for (Dot11SecurityInformationReport security : task.ssidReport().security()) {
                        String suite = Dot11.securitySuitesToIdentifier(security);
                        if (!monitoredSSID.securitySuites().contains(suite)) {
                            Map<String, String> attributes = Maps.newHashMap();
                            attributes.put("suite", suite);

                            nzyme.getDetectionAlertService().raiseAlert(
                                    task.tap().organizationId(),
                                    task.tap().tenantId(),
                                    monitoredSSID.uuid(),
                                    task.tap().uuid(),
                                    DetectionType.DOT11_MONITOR_SECURITY_SUITE,
                                    Subsystem.DOT11,
                                    "Monitored network \"" + monitoredSSID.ssid() + "\" advertised with " +
                                            "unexpected security suites \"" + suite + "\"",
                                    attributes,
                                    new String[]{"suite"},
                                    task.ssidReport().signalStrength().average()
                            );
                        }
                    }
                }
            }
        } catch(Exception e) {
            LOG.error("Could not write SSID.", e);
        }
    }

    private void writeDisco(Tap tap, DateTime timestamp, Dot11DiscoReport disco) {
        tablesService.getNzyme().getDatabase().useHandle(handle -> {
            for (Dot11DiscoTransmitterReport report : disco.deauthentication().values()) {
                writeDiscoReport(handle, tap, timestamp, Dot11.DiscoType.DEAUTHENTICATION, report);
            }

            for (Dot11DiscoTransmitterReport report : disco.disassociation().values()) {
                writeDiscoReport(handle, tap, timestamp, Dot11.DiscoType.DISASSOCIATION, report);
            }
        });
    }

    private void writeDiscoReport(Handle handle, Tap tap, DateTime timestamp, Dot11.DiscoType discoType, Dot11DiscoTransmitterReport report) {
        long activityId = handle.createQuery(
                "INSERT INTO dot11_disco_activity(tap_uuid, disco_type, bssid, sent_frames, " +
                        "created_at) VALUES(:tap_uuid, :disco_type, :bssid, :sent_frames, :created_at) " +
                        "RETURNING id")
                .bind("tap_uuid", tap.uuid())
                .bind("disco_type", discoType.getNumber())
                .bind("bssid", report.bssid())
                .bind("sent_frames", report.sentFrames())
                .bind("created_at", timestamp)
                .mapTo(Long.class)
                .one();

        PreparedBatch batch = handle.prepareBatch(
                "INSERT INTO dot11_disco_activity_receivers(disco_activity_id, bssid, received_frames)" +
                        " VALUES(:disco_activity_id, :bssid, :received_frames)"
        );

        for (Map.Entry<String, Long> receiver : report.receivers().entrySet()) {
                batch.bind("disco_activity_id", activityId)
                        .bind("bssid", receiver.getKey())
                        .bind("received_frames", receiver.getValue())
                        .add();
        }

        batch.execute();
    }

    private void handleAlerts(Tap tap, List<Dot11AlertReport> alerts) {
        for (Dot11AlertReport alert : alerts) {
            switch (alert.alertType()) {
                case PwnagotchiDetected:
                    Map<String, String> attributes = reportAlertAttributesToAlertAttributes(alert.attributes());
                    attributes.put("bandit_name", Dot11Bandits.CUSTOM_PWNAGOTCHI_NAME);
                    attributes.put("bandit_description", Dot11Bandits.CUSTOM_PWNAGOTCHI_DESCRIPTION);

                    tablesService.getNzyme().getDetectionAlertService().raiseAlert(
                            tap.organizationId(),
                            tap.tenantId(),
                            null,
                            tap.uuid(),
                            DetectionType.DOT11_BANDIT_CONTACT,
                            Subsystem.DOT11,
                            "Bandit \"Pwnagotchi\" with name \"" + attributes.get("name") + "\" detected in range.",
                            attributes,
                            new String[]{"identity"},
                            alert.signalStrength().floatValue()
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
        NzymeNode nzyme = tablesService.getNzyme();
        int dot11RetentionDays = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                .getValue(Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.key())
                .orElse(Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING"))
        );
        DateTime dot11CutOff = DateTime.now().minusDays(dot11RetentionDays);

        LOG.info("802.11/WiFi data retention: <{}> days / Delete data older than <{}>.",
                dot11RetentionDays, dot11CutOff);


    }

}
