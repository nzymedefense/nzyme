package app.nzyme.core.dot11.monitoring;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.db.*;
import app.nzyme.core.dot11.db.monitoring.*;
import app.nzyme.core.dot11.tracks.Track;
import app.nzyme.core.dot11.tracks.TrackDetector;
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

    private static final int MINUTES = 15;

    private final NzymeNode nzyme;

    public Dot11NetworkMonitor(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void run() {
        for (MonitoredSSID monitoredSSID : nzyme.getDot11().findAllMonitoredSSIDs(null, null)) {
            if (!monitoredSSID.isEnabled()) {
                LOG.debug("Skipping network monitor run for ssid [{}] because it is disabled.",
                        monitoredSSID.ssid());
                continue;
            }

            List<UUID> tapUUIDs = nzyme.getTapManager()
                    .allTapUUIDsAccessibleByScope(monitoredSSID.organizationId(), monitoredSSID.tenantId());

            // Detections.
            detectUnexpectedBSSIDs(monitoredSSID, tapUUIDs);
            detectUnexpectedChannels(monitoredSSID, tapUUIDs);
            detectUnexpectedSecuritySuites(monitoredSSID, tapUUIDs);
            detectUnexpectedFingerprints(monitoredSSID, tapUUIDs);
            detectUnexpectedSignalTracks(monitoredSSID, tapUUIDs);
        }

    }

    private void detectUnexpectedBSSIDs(MonitoredSSID monitoredSSID,
                                        List<UUID> tapUUIDs) {
        List<String> expectedBSSIDs = Lists.newArrayList();
        for (MonitoredBSSID monitoredBSSID : nzyme.getDot11().findMonitoredBSSIDsOfSSID(monitoredSSID.id())) {
            expectedBSSIDs.add(monitoredBSSID.bssid());
        }

        for (BSSIDSummary bssid : nzyme.getDot11().findBSSIDs(MINUTES, tapUUIDs)) {
            if (bssid.ssids().contains(monitoredSSID.ssid())) {
                // This is a BSSID advertising our network.
                if (!expectedBSSIDs.contains(bssid.bssid().toUpperCase())) {
                    // Alert.
                    LOG.info("BSSID [{}] advertising SSID [{}] is not in list of expected BSSIDs.",
                            bssid.bssid().toUpperCase(), monitoredSSID.ssid());
                    nzyme.getDot11().setMonitoredSSIDAlarmStatus(
                            monitoredSSID.id(),
                            Dot11.MonitoredNetworkAlertStatusColumn.UNEXPECTED_BSSID,
                            true
                    );
                    return;
                }
            }
        }

        // No alert.
        nzyme.getDot11().setMonitoredSSIDAlarmStatus(
                monitoredSSID.id(),
                Dot11.MonitoredNetworkAlertStatusColumn.UNEXPECTED_BSSID,
                false
        );
    }

    private void detectUnexpectedChannels(MonitoredSSID monitoredSSID,
                                          List<UUID> tapUUIDs) {
        List<Integer> expectedFrequencies = Lists.newArrayList();
        for (MonitoredChannel channel : nzyme.getDot11().findMonitoredChannelsOfMonitoredNetwork(monitoredSSID.id())) {
            expectedFrequencies.add((int) channel.frequency());
        }

        for (BSSIDSummary bssid : nzyme.getDot11().findBSSIDs(MINUTES, tapUUIDs)) {
            if (bssid.ssids().contains(monitoredSSID.ssid())) {
                // This is a BSSID advertising our network.

                for (SSIDChannelDetails ssid : nzyme.getDot11().findSSIDsOfBSSID(MINUTES, bssid.bssid(), tapUUIDs)) {
                    // Go through all channel/SSID combinations of the BSSID.
                    if (ssid.ssid().equals(monitoredSSID.ssid())) {
                        if (!expectedFrequencies.contains(ssid.frequency())) {
                            LOG.info("BSSID [{}] advertising SSID [{}] is using unexpected frequency [{}]",
                                    bssid.bssid().toUpperCase(), monitoredSSID.ssid(), ssid.frequency());

                            // Alert.
                            nzyme.getDot11().setMonitoredSSIDAlarmStatus(
                                    monitoredSSID.id(),
                                    Dot11.MonitoredNetworkAlertStatusColumn.UNEXPECTED_CHANNEL,
                                    true
                            );
                            return;
                        }
                    }
                }
            }
        }

        // No alert.
        nzyme.getDot11().setMonitoredSSIDAlarmStatus(
                monitoredSSID.id(),
                Dot11.MonitoredNetworkAlertStatusColumn.UNEXPECTED_CHANNEL,
                false
        );
    }

    private void detectUnexpectedSecuritySuites(MonitoredSSID monitoredSSID,
                                                List<UUID> tapUUIDs) {
        List<String> expectedSecurity = Lists.newArrayList();
        for (MonitoredSecuritySuite suite : nzyme.getDot11().findMonitoredSecuritySuitesOfMonitoredNetwork(monitoredSSID.id())) {
            expectedSecurity.add(suite.securitySuite());
        }

        ObjectMapper om = new ObjectMapper();

        for (BSSIDSummary bssid : nzyme.getDot11().findBSSIDs(MINUTES, tapUUIDs)) {
            if (bssid.ssids().contains(monitoredSSID.ssid())) {
                // This is a BSSID advertising our network.

                for (SSIDChannelDetails ssid : nzyme.getDot11().findSSIDsOfBSSID(MINUTES, bssid.bssid(), tapUUIDs)) {
                    // Go through all channel/SSID combinations of the BSSID.
                    if (ssid.ssid().equals(monitoredSSID.ssid())) {
                        Optional<SSIDDetails> ssidDetails = nzyme.getDot11().findSSIDDetails(MINUTES, bssid.bssid(), ssid.ssid(), tapUUIDs);

                        if (ssidDetails.isEmpty()) {
                            continue;
                        }

                        for (String suite : ssidDetails.get().securitySuites()) {
                            Dot11SecuritySuiteJson info;
                            try {
                                info = om.readValue(suite, Dot11SecuritySuiteJson.class);
                            } catch (JsonProcessingException e) {
                                LOG.error("Could not read SSID [{}] security suites.", ssid.ssid(), e);
                                continue;
                            }
                            if (!expectedSecurity.contains(Dot11.securitySuitesToIdentifier(info))) {
                                LOG.info("BSSID [{}] advertising SSID [{}] is using unexpected security suites [{}]",
                                        bssid.bssid().toUpperCase(), monitoredSSID.ssid(), suite);

                                // Alert.
                                nzyme.getDot11().setMonitoredSSIDAlarmStatus(
                                        monitoredSSID.id(),
                                        Dot11.MonitoredNetworkAlertStatusColumn.UNEXPECTED_SECURITY_SUITES,
                                        true
                                );
                                return;
                            }
                        }
                    }
                }
            }
        }

        // No alert.
        nzyme.getDot11().setMonitoredSSIDAlarmStatus(
                monitoredSSID.id(),
                Dot11.MonitoredNetworkAlertStatusColumn.UNEXPECTED_SECURITY_SUITES,
                false
        );
    }

    private void detectUnexpectedFingerprints(MonitoredSSID monitoredSSID,
                                              List<UUID> tapUUIDs) {
        Map<String, List<String>> expectedFingerprints = Maps.newHashMap();
        for (MonitoredBSSID monitoredBSSID : nzyme.getDot11().findMonitoredBSSIDsOfSSID(monitoredSSID.id())) {
            List<String> fps = Lists.newArrayList();
            for (MonitoredFingerprint fingerprint : nzyme.getDot11().findMonitoredFingerprintsOfMonitoredBSSID(monitoredBSSID.id())) {
                fps.add(fingerprint.fingerprint());
            }

            expectedFingerprints.put(monitoredBSSID.bssid(), fps);
        }

        for (BSSIDSummary bssid : nzyme.getDot11().findBSSIDs(MINUTES, tapUUIDs)) {
            if (bssid.ssids().contains(monitoredSSID.ssid())) {
                // This is a BSSID advertising our network.
                List<String> fps = expectedFingerprints.get(bssid.bssid());
                if (fps != null) {
                    for (String fingerprint : bssid.fingerprints()) {
                        if (!fps.contains(fingerprint)) {
                            LOG.info("BSSID [{}] advertising SSID [{}] has unexpected fingerprint [{}]",
                                    bssid.bssid().toUpperCase(), monitoredSSID.ssid(), fingerprint);

                            // Alert.
                            nzyme.getDot11().setMonitoredSSIDAlarmStatus(
                                    monitoredSSID.id(),
                                    Dot11.MonitoredNetworkAlertStatusColumn.UNEXPECTED_FINGERPRINT,
                                    true
                            );
                            return;
                        }
                    }
                }
            }
        }

        // No alert.
        nzyme.getDot11().setMonitoredSSIDAlarmStatus(
                monitoredSSID.id(),
                Dot11.MonitoredNetworkAlertStatusColumn.UNEXPECTED_FINGERPRINT,
                false
        );
    }

    private void detectUnexpectedSignalTracks(MonitoredSSID monitoredSSID, List<UUID> tapUUIDs) {
        for (MonitoredBSSID monitoredBSSID : nzyme.getDot11().findMonitoredBSSIDsOfSSID(monitoredSSID.id())) {
            for (MonitoredChannel monitoredChannel : nzyme.getDot11()
                    .findMonitoredChannelsOfMonitoredNetwork(monitoredSSID.id())) {
                List<ChannelHistogramEntry> signals = nzyme.getDot11().getSSIDSignalStrengthWaterfall(
                        monitoredBSSID.bssid(), monitoredSSID.ssid(), (int) monitoredChannel.frequency(), MINUTES, tapUUIDs);

                TrackDetector.TrackDetectorHeatmapData heatmap = TrackDetector.toChartAxisMaps(signals);

                TrackDetector td = new TrackDetector();
                List<Track> tracks = td.detect(heatmap.z(), heatmap.y(), TrackDetector.DEFAULT_CONFIG);

                // Alert if >1 track.
                if (tracks.size() > 1) {
                    nzyme.getDot11().setMonitoredSSIDAlarmStatus(
                            monitoredSSID.id(),
                            Dot11.MonitoredNetworkAlertStatusColumn.UNEXPECTED_SIGNAL_TRACKS,
                            true
                    );
                    return;
                }
            }
        }

        // No alert.
        nzyme.getDot11().setMonitoredSSIDAlarmStatus(
                monitoredSSID.id(),
                Dot11.MonitoredNetworkAlertStatusColumn.UNEXPECTED_SIGNAL_TRACKS,
                false
        );
    }

}
