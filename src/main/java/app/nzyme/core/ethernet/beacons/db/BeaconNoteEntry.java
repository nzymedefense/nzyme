package app.nzyme.core.ethernet.beacons.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class BeaconNoteEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract long beaconId();
    @Nullable
    public abstract UUID authorUserId();
    @Nullable
    public abstract String authorName();
    @Nullable
    public abstract String authorEmail();
    public abstract String note();
    public abstract DateTime createdAt();

    public static BeaconNoteEntry create(long id, UUID uuid, long beaconId, UUID authorUserId, String authorName, String authorEmail, String note, DateTime createdAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .beaconId(beaconId)
                .authorUserId(authorUserId)
                .authorName(authorName)
                .authorEmail(authorEmail)
                .note(note)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconNoteEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);
        public abstract Builder uuid(UUID uuid);
        public abstract Builder beaconId(long beaconId);
        public abstract Builder authorUserId(UUID authorUserId);
        public abstract Builder authorName(String authorName);
        public abstract Builder authorEmail(String authorEmail);
        public abstract Builder note(String note);
        public abstract Builder createdAt(DateTime createdAt);
        public abstract BeaconNoteEntry build();
    }

}
