package app.nzyme.core.rest.responses.dot11.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class MonitoredBSSIDDetailsResponse {

    @JsonProperty("ssid_uuid")
    public abstract UUID ssidUUID();

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("bssid")
    public abstract String bssid();

    @JsonProperty("bssid_oui")
    @Nullable
    public abstract String bssidOui();

    @JsonProperty("is_online")
    public abstract boolean isOnline();

    @JsonProperty("fingerprints")
    public abstract List<MonitoredFingerprintResponse> fingerprints();

    public static MonitoredBSSIDDetailsResponse create(UUID ssidUUID, UUID uuid, String bssid, String bssidOui, boolean isOnline, List<MonitoredFingerprintResponse> fingerprints) {
        return builder()
                .ssidUUID(ssidUUID)
                .uuid(uuid)
                .bssid(bssid)
                .bssidOui(bssidOui)
                .isOnline(isOnline)
                .fingerprints(fingerprints)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredBSSIDDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssidUUID(UUID ssidUUID);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder bssid(String bssid);

        public abstract Builder bssidOui(String bssidOui);

        public abstract Builder isOnline(boolean isOnline);

        public abstract Builder fingerprints(List<MonitoredFingerprintResponse> fingerprints);

        public abstract MonitoredBSSIDDetailsResponse build();
    }
}
