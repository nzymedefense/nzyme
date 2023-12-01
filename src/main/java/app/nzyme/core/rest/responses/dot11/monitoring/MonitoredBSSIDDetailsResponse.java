package app.nzyme.core.rest.responses.dot11.monitoring;

import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
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

    @JsonProperty("mac")
    public abstract Dot11MacAddressResponse mac();

    @JsonProperty("is_online")
    public abstract boolean isOnline();

    @JsonProperty("fingerprints")
    public abstract List<MonitoredFingerprintResponse> fingerprints();

    public static MonitoredBSSIDDetailsResponse create(UUID ssidUUID, UUID uuid, Dot11MacAddressResponse mac, boolean isOnline, List<MonitoredFingerprintResponse> fingerprints) {
        return builder()
                .ssidUUID(ssidUUID)
                .uuid(uuid)
                .mac(mac)
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

        public abstract Builder mac(Dot11MacAddressResponse mac);

        public abstract Builder isOnline(boolean isOnline);

        public abstract Builder fingerprints(List<MonitoredFingerprintResponse> fingerprints);

        public abstract MonitoredBSSIDDetailsResponse build();
    }
}
