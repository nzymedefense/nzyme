package app.nzyme.core.rest.responses.dot11.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class MonitoredFingerprintResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("fingerprint")
    public abstract String fingerprint();

    public static MonitoredFingerprintResponse create(UUID uuid, String fingerprint) {
        return builder()
                .uuid(uuid)
                .fingerprint(fingerprint)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredFingerprintResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder fingerprint(String fingerprint);

        public abstract MonitoredFingerprintResponse build();
    }
}
