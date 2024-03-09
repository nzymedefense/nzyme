package app.nzyme.core.util;

import com.google.auto.value.AutoValue;
import org.joda.time.Duration;

public class Bucketing {

    public enum Type {
        MINUTE("minute"),
        HOUR("hour"),
        DAY("day");

        private final String dateTruncName;

        Type(String dateTruncName) {
            this.dateTruncName = dateTruncName;
        }

        public String getDateTruncName() {
            return dateTruncName;
        }
    }

    public static BucketingConfiguration getConfig(TimeRange range) {
        if (range.isAllTime()) {
            return BucketingConfiguration.create(Type.DAY);
        }

        Duration duration = new Duration(range.from(), range.to());

        if (duration.getStandardHours() <= 24) {
            return BucketingConfiguration.create(Type.MINUTE);
        } else if (duration.getStandardDays() <= 60) {
            return BucketingConfiguration.create(Type.HOUR);
        } else {
            return BucketingConfiguration.create(Type.DAY);
        }
    }

    @AutoValue
    public abstract static class BucketingConfiguration {

        public abstract Type type();

        public static BucketingConfiguration create(Type type) {
            return builder()
                    .type(type)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_Bucketing_BucketingConfiguration.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder type(Type type);

            public abstract BucketingConfiguration build();
        }
    }

}
