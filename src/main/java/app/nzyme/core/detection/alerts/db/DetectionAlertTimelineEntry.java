package app.nzyme.core.detection.alerts.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DetectionAlertTimelineEntry {

    public abstract DateTime seenFrom();
    public abstract DateTime seenTo();

    public static DetectionAlertTimelineEntry create(DateTime seenFrom, DateTime seenTo) {
        return builder()
                .seenFrom(seenFrom)
                .seenTo(seenTo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DetectionAlertTimelineEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder seenFrom(DateTime seenFrom);

        public abstract Builder seenTo(DateTime seenTo);

        public abstract DetectionAlertTimelineEntry build();
    }
}
