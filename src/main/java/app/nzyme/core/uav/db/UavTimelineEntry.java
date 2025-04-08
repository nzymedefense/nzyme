package app.nzyme.core.uav.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class UavTimelineEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract DateTime seenFrom();
    public abstract DateTime seenTo();

    public static UavTimelineEntry create(long id, UUID uuid, DateTime seenFrom, DateTime seenTo) {
        return builder()
                .id(id)
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
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder seenFrom(DateTime seenFrom);

        public abstract Builder seenTo(DateTime seenTo);

        public abstract UavTimelineEntry build();
    }
}
