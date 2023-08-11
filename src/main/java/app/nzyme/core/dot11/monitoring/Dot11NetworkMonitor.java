package app.nzyme.core.dot11.monitoring;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.db.*;
import app.nzyme.core.dot11.db.monitoring.*;
import app.nzyme.core.dot11.tracks.Track;
import app.nzyme.core.dot11.tracks.TrackDetector;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Dot11NetworkMonitor {

    private static final Logger LOG = LogManager.getLogger(Dot11NetworkMonitor.class);

    private static final int MINUTES = 5;

    private final NzymeNode nzyme;

    private final Timer bssidMonitorTimer;
    private final Timer channelMonitorTimer;
    private final Timer securitySuitesMonitorTimer;
    private final Timer fingerprintMonitorTimer;
    private final Timer signalTrackMonitorTimer;

    public Dot11NetworkMonitor(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.bssidMonitorTimer = nzyme.getMetrics().timer(MetricNames.DOT11_MONITOR_BSSID);
        this.channelMonitorTimer = nzyme.getMetrics().timer(MetricNames.DOT11_MONITOR_CHANNEL);
        this.securitySuitesMonitorTimer = nzyme.getMetrics().timer(MetricNames.DOT11_MONITOR_SECURITY_SUITES);
        this.fingerprintMonitorTimer = nzyme.getMetrics().timer(MetricNames.DOT11_MONITOR_FINGERPRINT);
        this.signalTrackMonitorTimer = nzyme.getMetrics().timer(MetricNames.DOT11_MONITOR_SIGNAL_TRACKS);
    }

    public Map<Dot11NetworkMonitorType, Dot11NetworkMonitorResult> getAlertStatus(MonitoredSSID monitoredSSID) {
        Map<Dot11NetworkMonitorType, Dot11NetworkMonitorResult> result = Maps.newHashMap();

        if (!monitoredSSID.isEnabled()) {
            LOG.debug("Skipping network monitor run for ssid [{}] because it is disabled.",
                    monitoredSSID.ssid());
            return result;
        }

        List<UUID> tapUUIDs = nzyme.getTapManager()
                .allTapUUIDsAccessibleByScope(monitoredSSID.organizationId(), monitoredSSID.tenantId());

        // Detections.
        result.put(Dot11NetworkMonitorType.UNEXPECTED_BSSID, detectUnexpectedBSSIDs(monitoredSSID, tapUUIDs));
        result.put(Dot11NetworkMonitorType.UNEXPECTED_CHANNEL, detectUnexpectedChannels(monitoredSSID, tapUUIDs));
        result.put(Dot11NetworkMonitorType.UNEXPECTED_SECURITY_SUITES, detectUnexpectedSecuritySuites(monitoredSSID, tapUUIDs));
        result.put(Dot11NetworkMonitorType.UNEXPECTED_FINGERPRINT, detectUnexpectedFingerprints(monitoredSSID, tapUUIDs));
        result.put(Dot11NetworkMonitorType.UNEXPECTED_SIGNAL_TRACKS, detectUnexpectedSignalTracks(monitoredSSID, tapUUIDs));

        return result;
    }

    private Dot11NetworkMonitorResult detectUnexpectedBSSIDs(MonitoredSSID monitoredSSID, List<UUID> tapUUIDs) {
        try (Timer.Context ignored = bssidMonitorTimer.time()) {
            List<String> expectedBSSIDs = Lists.newArrayList();
            for (MonitoredBSSID monitoredBSSID : nzyme.getDot11().findMonitoredBSSIDsOfSSID(monitoredSSID.id())) {
                expectedBSSIDs.add(monitoredBSSID.bssid());
            }

            List<Object> unexpectedBSSIDs = Lists.newArrayList();
            for (String bssid : nzyme.getDot11().findAllBSSIDsAdvertisingSSID(MINUTES, monitoredSSID.ssid(), tapUUIDs)) {
                if (!expectedBSSIDs.contains(bssid.toUpperCase())) {
                    // Alert.
                    LOG.debug("BSSID [{}] advertising SSID [{}] is not in list of expected BSSIDs.",
                            bssid.toUpperCase(), monitoredSSID.ssid());

                    unexpectedBSSIDs.add(bssid);
                }
            }

            return Dot11NetworkMonitorResult.create(
                    Dot11NetworkMonitorType.UNEXPECTED_BSSID,
                    !unexpectedBSSIDs.isEmpty(),
                    unexpectedBSSIDs
            );
        }
    }

    private Dot11NetworkMonitorResult detectUnexpectedChannels(MonitoredSSID monitoredSSID, List<UUID> tapUUIDs) {
        try (Timer.Context ignored = channelMonitorTimer.time()) {
            List<String> monitoredBSSIDs = Lists.newArrayList();
            for (MonitoredBSSID monitoredBSSID : nzyme.getDot11().findMonitoredBSSIDsOfSSID(monitoredSSID.id())) {
                monitoredBSSIDs.add(monitoredBSSID.bssid());
            }

            List<Integer> expectedFrequencies = Lists.newArrayList();
            for (MonitoredChannel channel : nzyme.getDot11().findMonitoredChannelsOfMonitoredNetwork(monitoredSSID.id())) {
                expectedFrequencies.add((int) channel.frequency());
            }

            List<Object> unexpectedChannels = Lists.newArrayList();
            for (String bssid : monitoredBSSIDs) {
                for (int frequency : nzyme.getDot11().findChannelsOfSSIDOfBSSID(MINUTES, bssid, monitoredSSID.ssid(), tapUUIDs)) {
                    if (frequency == 0) {
                        continue;
                    }

                    if (!expectedFrequencies.contains(frequency)) {
                        LOG.debug("BSSID [{}] advertising SSID [{}] is using unexpected frequency [{}]",
                                bssid.toUpperCase(), monitoredSSID.ssid(), frequency);

                        // Alert.
                        if (!unexpectedChannels.contains(frequency)) {
                            unexpectedChannels.add(frequency);
                        }
                    }
                }
            }

            return Dot11NetworkMonitorResult.create(
                    Dot11NetworkMonitorType.UNEXPECTED_CHANNEL,
                    !unexpectedChannels.isEmpty(),
                    unexpectedChannels
            );
        }
    }

    private Dot11NetworkMonitorResult detectUnexpectedSecuritySuites(MonitoredSSID monitoredSSID, List<UUID> tapUUIDs) {
        try (Timer.Context ignored = securitySuitesMonitorTimer.time()) {
            List<String> monitoredBSSIDs = Lists.newArrayList();
            for (MonitoredBSSID monitoredBSSID : nzyme.getDot11().findMonitoredBSSIDsOfSSID(monitoredSSID.id())) {
                monitoredBSSIDs.add(monitoredBSSID.bssid());
            }

            ObjectMapper om = new ObjectMapper();

            List<String> expectedSecurity = Lists.newArrayList();
            for (MonitoredSecuritySuite suite : nzyme.getDot11().findMonitoredSecuritySuitesOfMonitoredNetwork(monitoredSSID.id())) {
                expectedSecurity.add(suite.securitySuite());
            }

            List<Object> unexpectedSecuritySuites = Lists.newArrayList();
            for (String bssid : monitoredBSSIDs) {
                List<String> securitySuites = nzyme.getDot11().findSecuritySuitesOfSSIDOfBSSID(MINUTES, bssid, monitoredSSID.ssid(), tapUUIDs);

                for (String suite : securitySuites) {
                    Dot11SecuritySuiteJson info;
                    try {
                        info = om.readValue(suite, Dot11SecuritySuiteJson.class);
                    } catch (JsonProcessingException e) {
                        LOG.error("Could not read SSID [{}] security suites.", monitoredSSID.ssid(), e);
                        continue;
                    }
                    if (!expectedSecurity.contains(Dot11.securitySuitesToIdentifier(info))) {
                        LOG.debug("BSSID [{}] advertising SSID [{}] is using unexpected security suites [{}]",
                                bssid.toUpperCase(), monitoredSSID.ssid(), suite);

                        String ssi = Dot11.securitySuitesToIdentifier(info);
                        if (!unexpectedSecuritySuites.contains(ssi)) {
                            unexpectedSecuritySuites.add(ssi);
                        }
                    }
                }
            }

            return Dot11NetworkMonitorResult.create(
                    Dot11NetworkMonitorType.UNEXPECTED_SECURITY_SUITES,
                    !unexpectedSecuritySuites.isEmpty(),
                    unexpectedSecuritySuites
            );
        }
    }

    private Dot11NetworkMonitorResult detectUnexpectedFingerprints(MonitoredSSID monitoredSSID, List<UUID> tapUUIDs) {
        List<MonitoredBSSID> monitoredBSSIDs = nzyme.getDot11().findMonitoredBSSIDsOfSSID(monitoredSSID.id());
        List<String> monitoredBSSIDsBSSIDs = Lists.newArrayList();
        try (Timer.Context ignored = fingerprintMonitorTimer.time()) {
            Map<String, List<String>> expectedFingerprints = Maps.newHashMap();
            for (MonitoredBSSID monitoredBSSID : monitoredBSSIDs) {
                monitoredBSSIDsBSSIDs.add(monitoredBSSID.bssid());

                List<String> fps = Lists.newArrayList();
                for (MonitoredFingerprint fingerprint : nzyme.getDot11().findMonitoredFingerprintsOfMonitoredBSSID(monitoredBSSID.id())) {
                    fps.add(fingerprint.fingerprint());
                }

                expectedFingerprints.put(monitoredBSSID.bssid(), fps);
            }

            Map<String, List<String>> unexpectedFingerprints = Maps.newHashMap();
            for (String bssid : monitoredBSSIDsBSSIDs) {
                List<String> fps = expectedFingerprints.get(bssid);
                if (fps != null) {
                    for (String fingerprint : nzyme.getDot11().findFingerprintsOfBSSID(MINUTES, bssid, tapUUIDs)) {
                        if (!fps.contains(fingerprint)) {
                            LOG.debug("BSSID [{}] advertising SSID [{}] has unexpected fingerprint [{}]",
                                    bssid.toUpperCase(), monitoredSSID.ssid(), fingerprint);

                            if (unexpectedFingerprints.containsKey(bssid)) {
                                unexpectedFingerprints.get(bssid).add(fingerprint);
                            } else {
                                List<String> fingerprints = Lists.newArrayList();
                                fingerprints.add(fingerprint);
                                unexpectedFingerprints.put(bssid, fingerprints);
                            }

                        }
                    }
                }
            }

            return Dot11NetworkMonitorResult.create(
                    Dot11NetworkMonitorType.UNEXPECTED_FINGERPRINT,
                    !unexpectedFingerprints.isEmpty(),
                    unexpectedFingerprints
            );
        }
    }

    private Dot11NetworkMonitorResult detectUnexpectedSignalTracks(MonitoredSSID monitoredSSID, List<UUID> tapUUIDs) {
        try (Timer.Context ignored = signalTrackMonitorTimer.time()) {
            List<Long> channels = Lists.newArrayList();
            for (MonitoredChannel channel : nzyme.getDot11().findMonitoredChannelsOfMonitoredNetwork(monitoredSSID.id())) {
                channels.add(channel.frequency());
            }

            Map<String, List<Integer>> affectedBSSIDs = Maps.newHashMap();
            /*for (BSSIDSummary bssid : nzyme.getDot11().findBSSIDs(MINUTES, tapUUIDs)) {
                if (bssid.ssids().contains(monitoredSSID.ssid())) {
                    // This is a BSSID advertising our network.

                    // Get all channels of this BSSID.
                    for (SSIDChannelDetails channel : nzyme.getDot11().findSSIDsOfBSSID(MINUTES, bssid.bssid(), tapUUIDs)) {
                        if(channels.contains((long) channel.frequency())) {
                            // This is a monitored frequency.
                            List<ChannelHistogramEntry> signals = nzyme.getDot11().getSSIDSignalStrengthWaterfall(
                                    bssid.bssid(), monitoredSSID.ssid(), channel.frequency(), MINUTES, tapUUIDs);

                            TrackDetector.TrackDetectorHeatmapData heatmap = TrackDetector.toChartAxisMaps(signals);

                            TrackDetector td = new TrackDetector();
                            List<Track> tracks = td.detect(heatmap.z(), heatmap.y(), TrackDetector.DEFAULT_CONFIG);

                            // Alert if >1 track.
                            if (tracks.size() > 1) {
                                LOG.debug("Frequency [{}] of BSSID [{}] advertising SSID [{}] has <{}> signal tracks.",
                                        channel.frequency(),
                                        bssid.bssid().toUpperCase(),
                                        monitoredSSID.ssid(),
                                        tracks.size());

                                if (affectedBSSIDs.containsKey(bssid.bssid())) {
                                    affectedBSSIDs.get(bssid.bssid()).add(channel.frequency());
                                } else {
                                    List<Integer> newChannels = Lists.newArrayList();
                                    newChannels.add(channel.frequency());
                                    affectedBSSIDs.put(bssid.bssid(), newChannels);
                                }
                            }
                        }
                    }
                }
            }*/

            return Dot11NetworkMonitorResult.create(
                    Dot11NetworkMonitorType.UNEXPECTED_SIGNAL_TRACKS,
                    !affectedBSSIDs.isEmpty(),
                    affectedBSSIDs
            );
        }
    }

    public static boolean isSSIDAlerted(Map<Dot11NetworkMonitorType, Dot11NetworkMonitorResult> status) {
        if (status.isEmpty()) {
            // Disabled monitor.
            return false;
        }

        for (Dot11NetworkMonitorType type : Dot11NetworkMonitorType.values()) {
            Dot11NetworkMonitorResult result = status.get(type);
            if (result == null) {
                throw new RuntimeException("802.11 Network Monitor did not return result for " +
                        "type [" + type.toString() + "].");
            }

            if (result.triggered()) {
                return true;
            }
        }


        return false;
    }

}
