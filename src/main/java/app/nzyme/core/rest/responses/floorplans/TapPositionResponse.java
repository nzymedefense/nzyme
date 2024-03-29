package app.nzyme.core.rest.responses.floorplans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class TapPositionResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("x")
    public abstract int x();

    @JsonProperty("y")
    public abstract int y();

    @Nullable
    @JsonProperty("last_report")
    public abstract DateTime lastReport();

    @JsonProperty("active")
    public abstract boolean active();

    public static TapPositionResponse create(UUID uuid, String name, int x, int y, DateTime lastReport, boolean active) {
        return builder()
                .uuid(uuid)
                .name(name)
                .x(x)
                .y(y)
                .lastReport(lastReport)
                .active(active)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapPositionResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract Builder x(int x);

        public abstract Builder y(int y);

        public abstract Builder lastReport(DateTime lastReport);

        public abstract Builder active(boolean active);

        public abstract TapPositionResponse build();
    }
}
