package app.nzyme.core.tables.dot11.monitoring;

import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class PreLoadedMonitoredSSID {

    public abstract UUID id();
    public abstract String ssid();
    public abstract Map<String, PreLoadedMonitoredBSSID> bssids();
    public abstract List<Integer> channels();
    public abstract List<String> securitySuites();

    public static PreLoadedMonitoredSSID create(UUID id, String ssid, Map<String, PreLoadedMonitoredBSSID> bssids, List<Integer> channels, List<String> securitySuites) {
        return builder()
                .id(id)
                .ssid(ssid)
                .bssids(bssids)
                .channels(channels)
                .securitySuites(securitySuites)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PreLoadedMonitoredSSID.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder ssid(String ssid);

        public abstract Builder bssids(Map<String, PreLoadedMonitoredBSSID> bssids);

        public abstract Builder channels(List<Integer> channels);

        public abstract Builder securitySuites(List<String> securitySuites);

        public abstract PreLoadedMonitoredSSID build();
    }
}
