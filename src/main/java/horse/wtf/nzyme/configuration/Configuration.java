package horse.wtf.nzyme.configuration;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;

import java.net.URI;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class Configuration {

    public abstract boolean versionchecksEnabled();
    public abstract boolean fetchOuis();

    public abstract Role role();
    public abstract String nzymeId();

    public abstract String databasePath();

    public abstract String pythonExecutable();
    public abstract String pythonScriptDirectory();
    public abstract String pythonScriptPrefix();

    public abstract URI restListenUri();

    public abstract List<Dot11MonitorDefinition> dot11Monitors();
    public abstract List<Dot11NetworkDefinition> dot11Networks();

    public abstract List<Alert.TYPE_WIDE> dot11Alerts();
    public abstract int alertingRetentionPeriodMinutes();
    public abstract int alertingTrainingPeriodSeconds();
    public abstract Map<String, BanditFingerprintDefinition> knownBanditFingerprints();

    public abstract List<GraylogAddress> graylogUplinks();

    public List<String> ourSSIDs() {
        ImmutableList.Builder<String> ssids = new ImmutableList.Builder<>();
        dot11Networks().forEach(n -> ssids.add(n.ssid()));
        return ssids.build();
    }

    public static Configuration create(boolean versionchecksEnabled, boolean fetchOuis, Role role, String nzymeId, String databasePath, String pythonExecutable, String pythonScriptDirectory, String pythonScriptPrefix, URI restListenUri, List<Dot11MonitorDefinition> dot11Monitors, List<Dot11NetworkDefinition> dot11Networks, List<Alert.TYPE_WIDE> dot11Alerts, int alertingRetentionPeriodMinutes, int alertingTrainingPeriodSeconds, Map<String, BanditFingerprintDefinition> knownBanditFingerprints, List<GraylogAddress> graylogUplinks) {
        return builder()
                .versionchecksEnabled(versionchecksEnabled)
                .fetchOuis(fetchOuis)
                .role(role)
                .nzymeId(nzymeId)
                .databasePath(databasePath)
                .pythonExecutable(pythonExecutable)
                .pythonScriptDirectory(pythonScriptDirectory)
                .pythonScriptPrefix(pythonScriptPrefix)
                .restListenUri(restListenUri)
                .dot11Monitors(dot11Monitors)
                .dot11Networks(dot11Networks)
                .dot11Alerts(dot11Alerts)
                .alertingRetentionPeriodMinutes(alertingRetentionPeriodMinutes)
                .alertingTrainingPeriodSeconds(alertingTrainingPeriodSeconds)
                .knownBanditFingerprints(knownBanditFingerprints)
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

        public abstract Builder databasePath(String databasePath);

        public abstract Builder pythonExecutable(String pythonExecutable);

        public abstract Builder pythonScriptDirectory(String pythonScriptDirectory);

        public abstract Builder pythonScriptPrefix(String pythonScriptPrefix);

        public abstract Builder restListenUri(URI restListenUri);

        public abstract Builder dot11Monitors(List<Dot11MonitorDefinition> dot11Monitors);

        public abstract Builder dot11Networks(List<Dot11NetworkDefinition> dot11Networks);

        public abstract Builder dot11Alerts(List<Alert.TYPE_WIDE> dot11Alerts);

        public abstract Builder alertingRetentionPeriodMinutes(int alertingRetentionPeriodMinutes);

        public abstract Builder alertingTrainingPeriodSeconds(int alertingTrainingPeriodSeconds);

        public abstract Builder knownBanditFingerprints(Map<String, BanditFingerprintDefinition> knownBanditFingerprints);

        public abstract Builder graylogUplinks(List<GraylogAddress> graylogUplinks);

        public abstract Configuration build();
    }

}
