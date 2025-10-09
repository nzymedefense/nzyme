package app.nzyme.core.rest.responses.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ThreeColumnTableHistogramResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("show_bar_chart")
    public abstract boolean showBarChart();

    @JsonProperty("values")
    public abstract List<ThreeColumnTableHistogramValueResponse> values();

    public static ThreeColumnTableHistogramResponse create(long total, boolean showBarChart, List<ThreeColumnTableHistogramValueResponse> values) {
        return builder()
                .total(total)
                .showBarChart(showBarChart)
                .values(values)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ThreeColumnTableHistogramResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder showBarChart(boolean showBarChart);

        public abstract Builder values(List<ThreeColumnTableHistogramValueResponse> values);

        public abstract ThreeColumnTableHistogramResponse build();
    }
}
