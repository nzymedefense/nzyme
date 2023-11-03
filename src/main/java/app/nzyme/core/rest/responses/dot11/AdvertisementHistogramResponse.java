package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.Map;

@AutoValue
public abstract class AdvertisementHistogramResponse {

    @JsonProperty("values")
    public abstract Map<DateTime, AdvertisementHistogramValueResponse> values();

    public static AdvertisementHistogramResponse create(Map<DateTime, AdvertisementHistogramValueResponse> values) {
        return builder()
                .values(values)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AdvertisementHistogramResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder values(Map<DateTime, AdvertisementHistogramValueResponse> values);

        public abstract AdvertisementHistogramResponse build();
    }
}
