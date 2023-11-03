package app.nzyme.core.rest.responses.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TwoColumnTableHistogramResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("values")
    public abstract List<TwoColumnTableHistogramValueResponse> values();

    public static TwoColumnTableHistogramResponse create(long total, List<TwoColumnTableHistogramValueResponse> values) {
        return builder()
                .total(total)
                .values(values)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TwoColumnTableHistogramResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder values(List<TwoColumnTableHistogramValueResponse> values);

        public abstract TwoColumnTableHistogramResponse build();
    }
}
