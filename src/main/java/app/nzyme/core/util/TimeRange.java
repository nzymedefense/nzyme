package app.nzyme.core.util;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class TimeRange {

    public abstract DateTime from();
    public abstract DateTime to();

    public abstract boolean isAllTime();

    public static TimeRange create(DateTime from, DateTime to, boolean isAllTime) {
        return builder()
                .from(from)
                .to(to)
                .isAllTime(isAllTime)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimeRange.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder from(DateTime from);

        public abstract Builder to(DateTime to);

        public abstract Builder isAllTime(boolean isAllTime);

        public abstract TimeRange build();
    }
}
