package app.nzyme.core.ethernet.beacons.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import jakarta.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class BeaconSourceEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract long beaconId();
    public abstract String sourceAddress();
    @Nullable
    public abstract String sourceMac();
    public abstract long checkinCount();
    public abstract double intervalSeconds();
    public abstract double intervalJitter();
    public abstract double regularity();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static BeaconSourceEntry create(long id, UUID uuid, long beaconId, String sourceAddress, String sourceMac, long checkinCount, double intervalSeconds, double intervalJitter, double regularity, DateTime firstSeen, DateTime lastSeen, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .beaconId(beaconId)
                .sourceAddress(sourceAddress)
                .sourceMac(sourceMac)
                .checkinCount(checkinCount)
                .intervalSeconds(intervalSeconds)
                .intervalJitter(intervalJitter)
                .regularity(regularity)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconSourceEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder beaconId(long beaconId);

        public abstract Builder sourceAddress(String sourceAddress);

        public abstract Builder sourceMac(String sourceMac);

        public abstract Builder checkinCount(long checkinCount);

        public abstract Builder intervalSeconds(double intervalSeconds);

        public abstract Builder intervalJitter(double intervalJitter);

        public abstract Builder regularity(double regularity);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract BeaconSourceEntry build();
    }
}
