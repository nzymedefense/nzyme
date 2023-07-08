package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.Map;

@AutoValue
public abstract class SSIDAdvertisementHistogramResponse {

    @JsonProperty("values")
    public abstract Map<DateTime, SSIDAdvertisementHistogramValueResponse> values();

    public static SSIDAdvertisementHistogramResponse create(Map<DateTime, SSIDAdvertisementHistogramValueResponse> values) {
        return builder()
                .values(values)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDAdvertisementHistogramResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder values(Map<DateTime, SSIDAdvertisementHistogramValueResponse> values);

        public abstract SSIDAdvertisementHistogramResponse build();
    }
}
