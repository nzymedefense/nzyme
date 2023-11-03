package app.nzyme.core.rest.responses.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TwoColumnTableHistogramValueResponse {

    @JsonProperty("column_one")
    public abstract HistogramValueStructureResponse columnOne();

    @JsonProperty("column_two")
    public abstract HistogramValueStructureResponse columnTwo();

    public static TwoColumnTableHistogramValueResponse create(HistogramValueStructureResponse columnOne, HistogramValueStructureResponse columnTwo) {
        return builder()
                .columnOne(columnOne)
                .columnTwo(columnTwo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TwoColumnTableHistogramValueResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder columnOne(HistogramValueStructureResponse columnOne);

        public abstract Builder columnTwo(HistogramValueStructureResponse columnTwo);

        public abstract TwoColumnTableHistogramValueResponse build();
    }
}
