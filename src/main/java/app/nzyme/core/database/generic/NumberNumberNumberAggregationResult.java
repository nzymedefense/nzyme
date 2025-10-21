package app.nzyme.core.database.generic;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class NumberNumberNumberAggregationResult {

    public abstract long key();
    public abstract long value1();
    public abstract long value2();

    public static NumberNumberNumberAggregationResult create(long key, long value1, long value2) {
        return builder()
                .key(key)
                .value1(value1)
                .value2(value2)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NumberNumberNumberAggregationResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder key(long key);

        public abstract Builder value1(long value1);

        public abstract Builder value2(long value2);

        public abstract NumberNumberNumberAggregationResult build();
    }

}
