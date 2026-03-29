package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;


@AutoValue
public abstract class UpdateMonitorRequest {

    @JsonProperty
    @NotEmpty
    public abstract String name();

    @JsonProperty
    @Nullable
    public abstract String description();

    @JsonProperty
    @Min(0)
    public abstract Integer triggerCondition();

    @JsonProperty
    @Min(1)
    public abstract Integer interval();

    @JsonCreator
    public static UpdateMonitorRequest create(@JsonProperty("name") String name,
                                              @JsonProperty("description") String description,
                                              @JsonProperty("trigger_condition") Integer triggerCondition,
                                              @JsonProperty("interval") Integer interval) {
        return builder()
                .name(name)
                .description(description)
                .triggerCondition(triggerCondition)
                .interval(interval)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateMonitorRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(@NotEmpty String name);

        public abstract Builder description(String description);

        public abstract Builder triggerCondition(@Min(0) Integer triggerCondition);

        public abstract Builder interval(@Min(1) Integer interval);

        public abstract UpdateMonitorRequest build();
    }
}
