package app.nzyme.core.rest.responses.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class Dot11MonitoredNetworkContextResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("is_enabled")
    public abstract boolean isEnabled();

    @JsonProperty("name")
    public abstract String name();

    public static Dot11MonitoredNetworkContextResponse create(UUID uuid, boolean isEnabled, String name) {
        return builder()
                .uuid(uuid)
                .isEnabled(isEnabled)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11MonitoredNetworkContextResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder isEnabled(boolean isEnabled);

        public abstract Builder name(String name);

        public abstract Dot11MonitoredNetworkContextResponse build();
    }
}
