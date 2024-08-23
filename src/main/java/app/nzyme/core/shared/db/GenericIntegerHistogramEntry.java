package app.nzyme.core.shared.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class GenericIntegerHistogramEntry {

    public abstract DateTime bucket();
    public abstract int value();

    public static GenericIntegerHistogramEntry create(DateTime bucket, int value) {
        return builder()
                .bucket(bucket)
                .value(value)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GenericIntegerHistogramEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder value(int value);

        public abstract GenericIntegerHistogramEntry build();
    }
}
