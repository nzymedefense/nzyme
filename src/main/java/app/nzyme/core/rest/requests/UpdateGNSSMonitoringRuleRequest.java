package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class UpdateGNSSMonitoringRuleRequest {

    @NotBlank
    public abstract String name();

    @Nullable
    public abstract String description();

    @NotEmpty
    public abstract Map<String, List<Object>> conditions();

    public abstract List<String> taps();

    @JsonCreator
    public static UpdateGNSSMonitoringRuleRequest create(@JsonProperty("name") String name,
                                                         @JsonProperty("description") String description,
                                                         @JsonProperty("conditions") Map<String, List<Object>> conditions,
                                                         @JsonProperty("taps") List<String> taps) {
        return builder()
                .name(name)
                .description(description)
                .conditions(conditions)
                .taps(taps)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateGNSSMonitoringRuleRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(@NotBlank String name);

        public abstract Builder description(String description);

        public abstract Builder conditions(@NotEmpty Map<String, List<Object>> conditions);

        public abstract Builder taps(List<String> taps);

        public abstract UpdateGNSSMonitoringRuleRequest build();
    }
}
