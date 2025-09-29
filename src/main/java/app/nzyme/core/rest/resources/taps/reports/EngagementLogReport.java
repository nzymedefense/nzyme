package app.nzyme.core.rest.resources.taps.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class EngagementLogReport {

    public abstract DateTime timestamp();
    public abstract String message();

    @JsonCreator
    public static EngagementLogReport create(@JsonProperty("timestamp") DateTime timestamp,
                                             @JsonProperty("message") String message) {
        return builder()
                .timestamp(timestamp)
                .message(message)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EngagementLogReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder message(String message);

        public abstract EngagementLogReport build();
    }
}
