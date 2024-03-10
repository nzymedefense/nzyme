package app.nzyme.core.rest.responses.dot11.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.Map;

@AutoValue
public abstract class ClientHistogramResponse {

    @JsonProperty("values")
    public abstract Map<DateTime, ClientHistogramValueResponse> connected();

    public static ClientHistogramResponse create(Map<DateTime, ClientHistogramValueResponse> connected) {
        return builder()
                .connected(connected)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientHistogramResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder connected(Map<DateTime, ClientHistogramValueResponse> connected);

        public abstract ClientHistogramResponse build();
    }
}
