package horse.wtf.nzyme.measurements;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class Measurement {

    public abstract MeasurementType type();
    public abstract long value();
    public abstract DateTime createdAt();

    public static Measurement create(MeasurementType type, long value, DateTime createdAt) {
        return builder()
                .type(type)
                .value(value)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Measurement.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(MeasurementType type);

        public abstract Builder value(long value);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Measurement build();
    }

}
