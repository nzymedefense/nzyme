package app.nzyme.core.database.generic;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class StringStringNumberAggregationResult {

    public abstract String key();
    public abstract String value1();
    public abstract long value2();

    public static StringStringNumberAggregationResult create(String key, String value1, long value2) {
        return builder()
                .key(key)
                .value1(value1)
                .value2(value2)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_StringStringNumberAggregationResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder key(String key);

        public abstract Builder value1(String value1);

        public abstract Builder value2(long value2);

        public abstract StringStringNumberAggregationResult build();
    }
}
