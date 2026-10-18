package app.nzyme.core.rest.responses.ethernet.tls;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class TLSAlertResponse {

    @JsonProperty("direction")
    public abstract String direction();

    @JsonProperty("level")
    public abstract int level();

    @JsonProperty("description")
    public abstract int description();

    @Nullable
    @JsonProperty("description_name")
    public abstract String descriptionName();

    @JsonProperty("encrypted")
    public abstract boolean encrypted();

    public static TLSAlertResponse create(String direction, int level, int description, String descriptionName, boolean encrypted) {
        return builder()
                .direction(direction)
                .level(level)
                .description(description)
                .descriptionName(descriptionName)
                .encrypted(encrypted)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSAlertResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder direction(String direction);
        public abstract Builder level(int level);
        public abstract Builder description(int description);
        public abstract Builder descriptionName(String descriptionName);
        public abstract Builder encrypted(boolean encrypted);
        public abstract TLSAlertResponse build();
    }
}
