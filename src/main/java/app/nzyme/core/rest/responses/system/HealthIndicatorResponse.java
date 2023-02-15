package app.nzyme.core.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class HealthIndicatorResponse {

    @JsonProperty("id")
    public abstract String id();
    @JsonProperty("name")
    public abstract String name();
    @JsonProperty("level")
    public abstract String level();
    @JsonProperty("last_checked")
    public abstract DateTime lastChecked();
    @JsonProperty("expired")
    public abstract boolean expired();
    @JsonProperty("active")
    public abstract boolean active();

    public static HealthIndicatorResponse create(String id, String name, String level, DateTime lastChecked, boolean expired, boolean active) {
        return builder()
                .id(id)
                .name(name)
                .level(level)
                .lastChecked(lastChecked)
                .expired(expired)
                .active(active)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_HealthIndicatorResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Builder name(String name);

        public abstract Builder level(String level);

        public abstract Builder lastChecked(DateTime lastChecked);

        public abstract Builder expired(boolean expired);

        public abstract Builder active(boolean active);

        public abstract HealthIndicatorResponse build();
    }
}
