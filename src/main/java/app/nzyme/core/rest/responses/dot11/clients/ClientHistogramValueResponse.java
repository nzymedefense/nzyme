package app.nzyme.core.rest.responses.dot11.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ClientHistogramValueResponse {

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    @JsonProperty("client_count")
    public abstract long clientCount();

    public static ClientHistogramValueResponse create(DateTime timestamp, long clientCount) {
        return builder()
                .timestamp(timestamp)
                .clientCount(clientCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientHistogramValueResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder clientCount(long clientCount);

        public abstract ClientHistogramValueResponse build();
    }
}
