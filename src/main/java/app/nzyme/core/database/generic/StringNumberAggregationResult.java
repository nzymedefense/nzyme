package app.nzyme.core.database.generic;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class StringNumberAggregationResult {

    public abstract String key();
    public abstract long value();

    public static StringNumberAggregationResult create(String key, long value) {
        return builder()
                .key(key)
                .value(value)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_StringNumberAggregationResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder key(String key);

        public abstract Builder value(long value);

        public abstract StringNumberAggregationResult build();
    }
}
