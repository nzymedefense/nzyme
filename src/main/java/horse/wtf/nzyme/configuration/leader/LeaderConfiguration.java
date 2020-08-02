package horse.wtf.nzyme.configuration.leader;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.service.callbacks.AlertCallback;
import horse.wtf.nzyme.configuration.Dot11MonitorDefinition;
import horse.wtf.nzyme.configuration.Dot11NetworkDefinition;
import horse.wtf.nzyme.configuration.Dot11TrapDeviceDefinition;
import horse.wtf.nzyme.configuration.TrackerDeviceConfiguration;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;

import javax.annotation.Nullable;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

@AutoValue
public abstract class LeaderConfiguration {

    public abstract boolean versionchecksEnabled();
    public abstract boolean fetchOuis();

    public abstract Role role();

    public abstract String adminPasswordHash();

    public abstract String databasePath();

    public abstract String pythonExecutable();
    public abstract String pythonScriptDirectory();
    public abstract String pythonScriptPrefix();

    public abstract URI restListenUri();
    public abstract URI httpExternalUri();

    public abstract boolean useTls();

    @Nullable
    public abstract Path tlsCertificatePath();

    @Nullable
    public abstract Path tlsKeyPath();

    public abstract List<Dot11MonitorDefinition> dot11Monitors();
    public abstract List<Dot11NetworkDefinition> dot11Networks();
    public abstract List<Dot11TrapDeviceDefinition> dot11TrapDevices();

    public abstract List<Alert.TYPE_WIDE> dot11Alerts();
    public abstract int alertingTrainingPeriodSeconds();

    public abstract List<GraylogAddress> graylogUplinks();

    public abstract List<AlertCallback> alertCallbacks();

    @Nullable
    public abstract Config debugConfig();

    @Nullable
    public abstract TrackerDeviceConfiguration trackerDevice();

    public List<String> ourSSIDs() {
        ImmutableList.Builder<String> ssids = new ImmutableList.Builder<>();
        dot11Networks().forEach(n -> ssids.add(n.ssid()));
        return ssids.build();
    }

    public static LeaderConfiguration create(boolean versionchecksEnabled, boolean fetchOuis, Role role, String adminPasswordHash, String databasePath, String pythonExecutable, String pythonScriptDirectory, String pythonScriptPrefix, URI restListenUri, URI httpExternalUri, boolean useTls, Path tlsCertificatePath, Path tlsKeyPath, List<Dot11MonitorDefinition> dot11Monitors, List<Dot11NetworkDefinition> dot11Networks, List<Dot11TrapDeviceDefinition> dot11TrapDevices, List<Alert.TYPE_WIDE> dot11Alerts, int alertingTrainingPeriodSeconds, List<GraylogAddress> graylogUplinks, List<AlertCallback> alertCallbacks, Config debugConfig, TrackerDeviceConfiguration trackerDevice) {
        return builder()
                .versionchecksEnabled(versionchecksEnabled)
                .fetchOuis(fetchOuis)
                .role(role)
                .adminPasswordHash(adminPasswordHash)
                .databasePath(databasePath)
                .pythonExecutable(pythonExecutable)
                .pythonScriptDirectory(pythonScriptDirectory)
                .pythonScriptPrefix(pythonScriptPrefix)
                .restListenUri(restListenUri)
                .httpExternalUri(httpExternalUri)
                .useTls(useTls)
                .tlsCertificatePath(tlsCertificatePath)
                .tlsKeyPath(tlsKeyPath)
                .dot11Monitors(dot11Monitors)
                .dot11Networks(dot11Networks)
                .dot11TrapDevices(dot11TrapDevices)
                .dot11Alerts(dot11Alerts)
                .alertingTrainingPeriodSeconds(alertingTrainingPeriodSeconds)
                .graylogUplinks(graylogUplinks)
                .alertCallbacks(alertCallbacks)
                .debugConfig(debugConfig)
                .trackerDevice(trackerDevice)
                .build();
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

    public static Builder builder() {
        return new AutoValue_LeaderConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder versionchecksEnabled(boolean versionchecksEnabled);

        public abstract Builder fetchOuis(boolean fetchOuis);

        public abstract Builder role(Role role);

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

        public abstract Builder dot11TrapDevices(List<Dot11TrapDeviceDefinition> dot11TrapDevices);

        public abstract Builder dot11Alerts(List<Alert.TYPE_WIDE> dot11Alerts);

        public abstract Builder alertingTrainingPeriodSeconds(int alertingTrainingPeriodSeconds);

        public abstract Builder graylogUplinks(List<GraylogAddress> graylogUplinks);

        public abstract Builder alertCallbacks(List<AlertCallback> alertCallbacks);

        public abstract Builder debugConfig(Config debugConfig);

        public abstract Builder trackerDevice(TrackerDeviceConfiguration trackerDevice);

        public abstract LeaderConfiguration build();
    }
}