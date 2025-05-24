package app.nzyme.core.uav.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class UavTimelineEntry {

    public abstract UUID uuid();
    public abstract DateTime seenFrom();
    public abstract DateTime seenTo();

    public static UavTimelineEntry create(UUID uuid, DateTime seenFrom, DateTime seenTo) {
        return builder()
                .uuid(uuid)
                .seenFrom(seenFrom)
                .seenTo(seenTo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavTimelineEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder seenFrom(DateTime seenFrom);

        public abstract Builder seenTo(DateTime seenTo);

        public abstract UavTimelineEntry build();
    }
}
