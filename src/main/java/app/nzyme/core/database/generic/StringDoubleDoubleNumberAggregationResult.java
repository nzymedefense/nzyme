package app.nzyme.core.database.generic;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class StringDoubleDoubleNumberAggregationResult {

    public abstract String key();
    public abstract double value1();
    public abstract double value2();

    public static StringDoubleDoubleNumberAggregationResult create(String key, double value1, double value2) {
        return builder()
                .key(key)
                .value1(value1)
                .value2(value2)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_StringDoubleDoubleNumberAggregationResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder key(String key);

        public abstract Builder value1(double value1);

        public abstract Builder value2(double value2);

        public abstract StringDoubleDoubleNumberAggregationResult build();
    }
}
