package app.nzyme.core.rest.responses.dot11.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.Map;

@AutoValue
public abstract class ClientHistogramsResponse {

    @JsonProperty("connected")
    public abstract Map<DateTime, ClientHistogramValueResponse> connected();

    @JsonProperty("disconnected")
    public abstract Map<DateTime, ClientHistogramValueResponse> disconnected();

    public static ClientHistogramsResponse create(Map<DateTime, ClientHistogramValueResponse> connected, Map<DateTime, ClientHistogramValueResponse> disconnected) {
        return builder()
                .connected(connected)
                .disconnected(disconnected)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientHistogramsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder connected(Map<DateTime, ClientHistogramValueResponse> connected);

        public abstract Builder disconnected(Map<DateTime, ClientHistogramValueResponse> disconnected);

        public abstract ClientHistogramsResponse build();
    }

}
