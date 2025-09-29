package app.nzyme.core.rest.responses.taps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class TapEngagementLogDetailsResponse {

    @JsonProperty("message")
    public abstract String message();

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    public static TapEngagementLogDetailsResponse create(String message, DateTime timestamp) {
        return builder()
                .message(message)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapEngagementLogDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder message(String message);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract TapEngagementLogDetailsResponse build();
    }
}
