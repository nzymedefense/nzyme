package app.nzyme.core.util;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class TimeRange {

    public abstract DateTime from();
    public abstract DateTime to();

    public static TimeRange create(DateTime from, DateTime to) {
        return builder()
                .from(from)
                .to(to)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimeRange.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder from(DateTime from);

        public abstract Builder to(DateTime to);

        public abstract TimeRange build();
    }
}
