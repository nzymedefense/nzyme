package horse.wtf.nzyme.configuration;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;

import javax.annotation.Nullable;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class Configuration {

    public abstract boolean versionchecksEnabled();
    public abstract boolean fetchOuis();

    public abstract Role role();
    public abstract String nzymeId();

    public abstract String adminPasswordHash();

    public abstract String databasePath();

    public abstract String pythonExecutable();
    public abstract String pythonScriptDirectory();
    public abstract String pythonScriptPrefix();

    public abstract URI restListenUri();
    public abstract URI httpExternalUri();

    public abstract boolean useTls();
    public abstract Path tlsCertificatePath();
    public abstract Path tlsKeyPath();

    public abstract List<Dot11MonitorDefinition> dot11Monitors();
    public abstract List<Dot11NetworkDefinition> dot11Networks();

    public abstract List<Alert.TYPE_WIDE> dot11Alerts();
    public abstract int alertingRetentionPeriodMinutes();
    public abstract int alertingTrainingPeriodSeconds();
    public abstract Map<String, BanditFingerprintDefinition> knownBanditFingerprints();

    public abstract int signalQualityTableSizeMinutes();
    public abstract double expectedSignalDeltaModifier();
    public abstract int anomalyAlertLookbackMinutes();
    public abstract double anomalyAlertTriggerRatio();

    public abstract List<GraylogAddress> graylogUplinks();

    public List<String> ourSSIDs() {
        ImmutableList.Builder<String> ssids = new ImmutableList.Builder<>();
        dot11Networks().forEach(n -> ssids.add(n.ssid()));
        return ssids.build();
    }

    @Nullable
    public Dot11NetworkDefinition findNetworkDefinition(String bssid, String ssid) {
        for (Dot11NetworkDefinition network : dot11Networks()) {
            if (network.allBSSIDAddresses().contains(bssid) && network.ssid().equals(ssid)) {
                return network;
            }
        }

        return null;
    }

    public static Configuration create(boolean versionchecksEnabled,
                                       boolean fetchOuis,
                                       Role role,
                                       String nzymeId,
                                       String adminPasswordHash,
                                       String databasePath,
                                       String pythonExecutable,
                                       String pythonScriptDirectory,
                                       String pythonScriptPrefix,
                                       boolean useTls,
                                       Path tlsCertificatePath,
                                       Path tlsKeyPath,
                                       URI restListenUri,
                                       URI httpExternalUri,
                                       List<Dot11MonitorDefinition> dot11Monitors,
                                       List<Dot11NetworkDefinition> dot11Networks,
                                       List<Alert.TYPE_WIDE> dot11Alerts,
                                       int alertingRetentionPeriodMinutes,
                                       int alertingTrainingPeriodSeconds,
                                       Map<String, BanditFingerprintDefinition> knownBanditFingerprints,
                                       int signalQualityTableSizeMinutes,
                                       double expectedSignalDeltaModifier,
                                       int anomalyAlertLookbackMinutes,
                                       double anomalyAlertTriggerRatio,
                                       List<GraylogAddress> graylogUplinks) {
        return builder()
                .versionchecksEnabled(versionchecksEnabled)
                .fetchOuis(fetchOuis)
                .role(role)
                .nzymeId(nzymeId)
                .adminPasswordHash(adminPasswordHash)
                .databasePath(databasePath)
                .pythonExecutable(pythonExecutable)
                .pythonScriptDirectory(pythonScriptDirectory)
                .pythonScriptPrefix(pythonScriptPrefix)
                .useTls(useTls)
                .tlsCertificatePath(tlsCertificatePath)
                .tlsKeyPath(tlsKeyPath)
                .restListenUri(restListenUri)
                .httpExternalUri(httpExternalUri)
                .dot11Monitors(dot11Monitors)
                .dot11Networks(dot11Networks)
                .dot11Alerts(dot11Alerts)
                .alertingRetentionPeriodMinutes(alertingRetentionPeriodMinutes)
                .alertingTrainingPeriodSeconds(alertingTrainingPeriodSeconds)
                .knownBanditFingerprints(knownBanditFingerprints)
                .signalQualityTableSizeMinutes(signalQualityTableSizeMinutes)
                .expectedSignalDeltaModifier(expectedSignalDeltaModifier)
                .anomalyAlertLookbackMinutes(anomalyAlertLookbackMinutes)
                .anomalyAlertTriggerRatio(anomalyAlertTriggerRatio)
                .graylogUplinks(graylogUplinks)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Configuration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder versionchecksEnabled(boolean versionchecksEnabled);

        public abstract Builder fetchOuis(boolean fetchOuis);

        public abstract Builder role(Role role);

        public abstract Builder nzymeId(String nzymeId);

        public abstract Builder adminPasswordHash(String adminPasswordHash);

        public abstract Builder databasePath(String databasePath);

        public abstract Builder pythonExecutable(String pythonExecutable);

        public abstract Builder pythonScriptDirectory(String pythonScriptDirectory);

        public abstract Builder pythonScriptPrefix(String pythonScriptPrefix);

        public abstract Builder restListenUri(URI restListenUri);

        public abstract Builder httpExternalUri(URI httpExternalUri);

        public abstract Builder useTls(boolean useTls);

        public abstract Builder tlsCertificatePath(Path tlsCertificatePath);

        public abstract Builder tlsKeyPath(Path tlsKeyPath);

        public abstract Builder dot11Monitors(List<Dot11MonitorDefinition> dot11Monitors);

        public abstract Builder dot11Networks(List<Dot11NetworkDefinition> dot11Networks);

        public abstract Builder dot11Alerts(List<Alert.TYPE_WIDE> dot11Alerts);

        public abstract Builder alertingRetentionPeriodMinutes(int alertingRetentionPeriodMinutes);

        public abstract Builder alertingTrainingPeriodSeconds(int alertingTrainingPeriodSeconds);

        public abstract Builder knownBanditFingerprints(Map<String, BanditFingerprintDefinition> knownBanditFingerprints);

        public abstract Builder signalQualityTableSizeMinutes(int signalQualityTableSizeMinutes);

        public abstract Builder expectedSignalDeltaModifier(double expectedSignalDeltaModifier);

        public abstract Builder anomalyAlertLookbackMinutes(int anomalyAlertLookbackMinutes);

        public abstract Builder anomalyAlertTriggerRatio(double anomalyAlertTriggerRatio);

        public abstract Builder graylogUplinks(List<GraylogAddress> graylogUplinks);

        public abstract Configuration build();
    }

}