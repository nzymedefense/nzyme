package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;


@AutoValue
public abstract class UpdateMonitorRequest {

    @Nullable
    public abstract String name();

    @Nullable
    public abstract String description();

    @Nullable
    public abstract Integer triggerCondition();

    @Nullable
    public abstract Integer interval();

    @Nullable
    public abstract String filters();

    @Nullable
    public abstract List<String> taps();

    @JsonCreator
    public static UpdateMonitorRequest create(@JsonProperty("name") String name,
                                              @JsonProperty("description") String description,
                                              @JsonProperty("trigger_condition") Integer triggerCondition,
                                              @JsonProperty("interval") Integer interval,
                                              @JsonProperty("filters") String filters,
                                              @JsonProperty("taps") List<String> taps) {
        return builder()
                .name(name)
                .description(description)
                .triggerCondition(triggerCondition)
                .interval(interval)
                .filters(filters)
                .taps(taps)
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

        public abstract Builder filters(String filters);

        public abstract Builder taps(List<String> taps);

        public abstract UpdateMonitorRequest build();
    }
}
