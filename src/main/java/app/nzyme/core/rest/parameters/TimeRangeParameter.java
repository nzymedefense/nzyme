package app.nzyme.core.rest.parameters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotEmpty;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class TimeRangeParameter {

    @NotEmpty
    public abstract String type();

    @Nullable
    public abstract Integer minutes();

    @Nullable
    public abstract String name();

    @Nullable
    public abstract DateTime from();

    @Nullable
    public abstract DateTime to();

    @JsonCreator
    public static TimeRangeParameter create(@JsonProperty("type") String type,
                                            @JsonProperty("minutes") Integer minutes,
                                            @JsonProperty("name") String name,
                                            @JsonProperty("from") DateTime from,
                                            @JsonProperty("to") DateTime to) {
        return builder()
                .type(type)
                .minutes(minutes)
                .name(name)
                .from(from)
                .to(to)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimeRangeParameter.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(@NotEmpty String type);

        public abstract Builder minutes(Integer minutes);

        public abstract Builder name(String name);

        public abstract Builder from(DateTime from);

        public abstract Builder to(DateTime to);

        public abstract TimeRangeParameter build();
    }
}
