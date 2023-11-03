package app.nzyme.core.rest.responses.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ThreeColumnTableHistogramValueResponse {

    @JsonProperty("column_one")
    public abstract HistogramValueStructureResponse columnOne();

    @JsonProperty("column_two")
    public abstract HistogramValueStructureResponse columnTwo();

    @JsonProperty("column_three")
    public abstract HistogramValueStructureResponse columnThree();

    // For charts etc that need a single key to display for grouping values
    @JsonProperty("key_summary")
    public abstract String keySummary();

    public static ThreeColumnTableHistogramValueResponse create(HistogramValueStructureResponse columnOne, HistogramValueStructureResponse columnTwo, HistogramValueStructureResponse columnThree, String keySummary) {
        return builder()
                .columnOne(columnOne)
                .columnTwo(columnTwo)
                .columnThree(columnThree)
                .keySummary(keySummary)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ThreeColumnTableHistogramValueResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder columnOne(HistogramValueStructureResponse columnOne);

        public abstract Builder columnTwo(HistogramValueStructureResponse columnTwo);

        public abstract Builder columnThree(HistogramValueStructureResponse columnThree);

        public abstract Builder keySummary(String keySummary);

        public abstract ThreeColumnTableHistogramValueResponse build();
    }
}
