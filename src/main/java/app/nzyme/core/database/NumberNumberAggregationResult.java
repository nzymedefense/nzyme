package app.nzyme.core.database;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class NumberNumberAggregationResult {

    public abstract long key();
    public abstract long value();

    public static NumberNumberAggregationResult create(long key, long value) {
        return builder()
                .key(key)
                .value(value)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NumberNumberAggregationResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder key(long key);

        public abstract Builder value(long value);

        public abstract NumberNumberAggregationResult build();
    }
}
