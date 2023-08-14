package app.nzyme.core.detection.alerts;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.monitoring.MonitoredSSID;
import app.nzyme.core.dot11.monitoring.Dot11BanditDetector;
import app.nzyme.core.dot11.monitoring.Dot11NetworkMonitor;
import app.nzyme.core.dot11.monitoring.Dot11NetworkMonitorResult;
import app.nzyme.core.dot11.monitoring.Dot11NetworkMonitorType;
import app.nzyme.core.util.Tools;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DetectionAlertMonitor {

    private static final Logger LOG = LogManager.getLogger(DetectionAlertMonitor.class);

    private final NzymeNode nzyme;

    public DetectionAlertMonitor(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void initialize() {
        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("alert-monitor-%d")
                        .build()
        ).scheduleAtFixedRate(this::run, 30, 30, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private void run() {
        try {
            LOG.debug("Alert monitor run started.");

            // Run 802.11 network monitor for every monitored network.
            Dot11NetworkMonitor dot11NetworkMonitor = new Dot11NetworkMonitor(nzyme);
            for (MonitoredSSID monitoredSSID : nzyme.getDot11().findAllMonitoredSSIDs(null, null)) {
                Map<Dot11NetworkMonitorType, Dot11NetworkMonitorResult> alerts =
                        dot11NetworkMonitor.getAlertStatus(monitoredSSID);

                for (Map.Entry<Dot11NetworkMonitorType, Dot11NetworkMonitorResult> alert : alerts.entrySet()) {
                    if (!alert.getValue().triggered()) {
                        continue;
                    }

                    switch (alert.getKey()) {
                        case UNEXPECTED_BSSID:
                            List<String> bssids = (List<String>) alert.getValue().deviatedValues();
                            for (String bssid : bssids) {
                                Map<String, String> attributes = Maps.newHashMap();
                                attributes.put("bssid", bssid);

                                nzyme.getDetectionAlertService().raiseAlert(
                                        monitoredSSID.organizationId(),
                                        monitoredSSID.tenantId(),
                                        monitoredSSID.uuid(),
                                        null,
                                        DetectionType.DOT11_MONITOR_BSSID,
                                        Subsystem.DOT11,
                                        "Monitored network \"" + monitoredSSID.ssid() + "\" advertised with " +
                                                "unexpected BSSID \"" + bssid + "\"",
                                        attributes,
                                        new String[]{"bssid"},
                                        null
                                );
                            }
                            break;
                        case UNEXPECTED_CHANNEL:
                            List<Integer> frequencies = (List<Integer>) alert.getValue().deviatedValues();
                            for (int frequency : frequencies) {
                                Map<String, String> attributes = Maps.newHashMap();
                                attributes.put("frequency", String.valueOf(frequency));

                                nzyme.getDetectionAlertService().raiseAlert(
                                        monitoredSSID.organizationId(),
                                        monitoredSSID.tenantId(),
                                        monitoredSSID.uuid(),
                                        null,
                                        DetectionType.DOT11_MONITOR_CHANNEL,
                                        Subsystem.DOT11,
                                        "Monitored network \"" + monitoredSSID.ssid() + "\" advertised on " +
                                                "unexpected frequency " + frequency + "MHz",
                                        attributes,
                                        new String[]{"frequency"},
                                        null
                                );
                            }
                            break;
                        case UNEXPECTED_SECURITY_SUITES:
                            List<String> suites = (List<String>) alert.getValue().deviatedValues();
                            for (String suite : suites) {
                                Map<String, String> attributes = Maps.newHashMap();
                                attributes.put("suite", suite);

                                nzyme.getDetectionAlertService().raiseAlert(
                                        monitoredSSID.organizationId(),
                                        monitoredSSID.tenantId(),
                                        monitoredSSID.uuid(),
                                        null,
                                        DetectionType.DOT11_MONITOR_SECURITY_SUITE,
                                        Subsystem.DOT11,
                                        "Monitored network \"" + monitoredSSID.ssid() + "\" advertised with " +
                                                "unexpected security suites \"" + suite + "\"",
                                        attributes,
                                        new String[]{"suite"},
                                        null
                                );
                            }
                            break;
                        case UNEXPECTED_FINGERPRINT:
                            Map<String, List<String>> bssidFingerprints =
                                    (Map<String, List<String>> ) alert.getValue().deviatedValues();
                            for (Map.Entry<String, List<String>> bssidFp : bssidFingerprints.entrySet()) {
                                for (String fingerprint : bssidFp.getValue()) {
                                    Map<String, String> attributes = Maps.newHashMap();
                                    attributes.put("bssid", bssidFp.getKey());
                                    attributes.put("fingerprint", fingerprint);

                                    nzyme.getDetectionAlertService().raiseAlert(
                                            monitoredSSID.organizationId(),
                                            monitoredSSID.tenantId(),
                                            monitoredSSID.uuid(),
                                            null,
                                            DetectionType.DOT11_MONITOR_FINGERPRINT,
                                            Subsystem.DOT11,
                                            "Monitored network \"" + monitoredSSID.ssid() + "\" advertised " +
                                                    "with unexpected fingerprint \"" + fingerprint + "\".",
                                            attributes,
                                            new String[]{"bssid", "fingerprint"},
                                            null
                                    );
                                }
                            }
                            break;
                        case UNEXPECTED_SIGNAL_TRACKS:
                            Map<String, List<Integer>> bssidChannels =
                                    (Map<String, List<Integer>> ) alert.getValue().deviatedValues();
                            for (Map.Entry<String, List<Integer>> bssidChannel : bssidChannels.entrySet()) {
                                for (Integer channel : bssidChannel.getValue()) {
                                    Map<String, String> attributes = Maps.newHashMap();
                                    attributes.put("bssid", bssidChannel.getKey());
                                    attributes.put("channel", String.valueOf(channel));

                                    nzyme.getDetectionAlertService().raiseAlert(
                                            monitoredSSID.organizationId(),
                                            monitoredSSID.tenantId(),
                                            monitoredSSID.uuid(),
                                            null,
                                            DetectionType.DOT11_MONITOR_FINGERPRINT,
                                            Subsystem.DOT11,
                                            "Monitored network \"" + monitoredSSID.ssid() + "\" advertised " +
                                                    "with multiple signal tracks on channel \"" + channel + "\".",
                                            attributes,
                                            new String[]{"bssid", "channel"},
                                            null
                                    );
                                }
                                }
                            break;
                    }
                }
            }
        } catch(Exception e) {
            LOG.error("Could not complete detection alert monitor run.", e);
        }

        // Run bandit detection.
        try {
            new Dot11BanditDetector(nzyme).run();
        } catch(Exception e) {
            LOG.error("Could not complete bandit detector run.", e);
        }
    }

}
