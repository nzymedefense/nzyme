package app.nzyme.core.rest.responses.ethernet.dhcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DHCPTimelineStepResponse {

    @JsonProperty("step")
    public abstract String step();

    @JsonProperty("timestamp")
    public abstract String timestamp(); // String not DateTime, because we need microsecond resolution.

    public static DHCPTimelineStepResponse create(String step, String timestamp) {
        return builder()
                .step(step)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DHCPTimelineStepResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder step(String step);

        public abstract Builder timestamp(String timestamp);

        public abstract DHCPTimelineStepResponse build();
    }
}
