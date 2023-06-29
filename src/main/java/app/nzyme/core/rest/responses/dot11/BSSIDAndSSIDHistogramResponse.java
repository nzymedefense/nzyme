package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.Map;

@AutoValue
public abstract class BSSIDAndSSIDHistogramResponse {

    @JsonProperty("values")
    public abstract Map<DateTime, BSSIDAndSSIDHistogramValueResponse> values();

    public static BSSIDAndSSIDHistogramResponse create(Map<DateTime, BSSIDAndSSIDHistogramValueResponse> values) {
        return builder()
                .values(values)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDAndSSIDHistogramResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder values(Map<DateTime, BSSIDAndSSIDHistogramValueResponse> values);

        public abstract BSSIDAndSSIDHistogramResponse build();
    }
}
