package app.nzyme.core.tables.dot11.monitoring;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class PreLoadedMonitoredBSSID {

    public abstract String bssid();
    public abstract List<String> fingerprints();

    public static PreLoadedMonitoredBSSID create(String bssid, List<String> fingerprints) {
        return builder()
                .bssid(bssid)
                .fingerprints(fingerprints)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PreLoadedMonitoredBSSID.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(String bssid);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract PreLoadedMonitoredBSSID build();
    }
}
