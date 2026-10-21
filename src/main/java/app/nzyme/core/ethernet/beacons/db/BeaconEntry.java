package app.nzyme.core.ethernet.beacons.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import jakarta.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class BeaconEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract String type();
    public abstract String destinationKey();
    @Nullable
    public abstract String destinationHost();
    @Nullable
    public abstract String destinationAddress();
    @Nullable
    public abstract Integer destinationPort();
    public abstract boolean destinationIsSiteLocal();
    public abstract String name();
    @Nullable
    public abstract String description();
    public abstract String classification();
    @Nullable
    public abstract DateTime classificationChangedAt();
    public abstract int sourceCount();
    public abstract long checkinCount();
    public abstract double intervalSeconds();
    public abstract double intervalJitter();
    @Nullable
    public abstract String enrichment();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static BeaconEntry create(long id, UUID uuid, UUID organizationId, UUID tenantId, String type, String destinationKey, String destinationHost, String destinationAddress, Integer destinationPort, boolean destinationIsSiteLocal, String name, String description, String classification, DateTime classificationChangedAt, int sourceCount, long checkinCount, double intervalSeconds, double intervalJitter, String enrichment, DateTime firstSeen, DateTime lastSeen, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .type(type)
                .destinationKey(destinationKey)
                .destinationHost(destinationHost)
                .destinationAddress(destinationAddress)
                .destinationPort(destinationPort)
                .destinationIsSiteLocal(destinationIsSiteLocal)
                .name(name)
                .description(description)
                .classification(classification)
                .classificationChangedAt(classificationChangedAt)
                .sourceCount(sourceCount)
                .checkinCount(checkinCount)
                .intervalSeconds(intervalSeconds)
                .intervalJitter(intervalJitter)
                .enrichment(enrichment)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder type(String type);

        public abstract Builder destinationKey(String destinationKey);

        public abstract Builder destinationHost(String destinationHost);

        public abstract Builder destinationAddress(String destinationAddress);

        public abstract Builder destinationPort(Integer destinationPort);

        public abstract Builder destinationIsSiteLocal(boolean destinationIsSiteLocal);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder classification(String classification);

        public abstract Builder classificationChangedAt(DateTime classificationChangedAt);

        public abstract Builder sourceCount(int sourceCount);

        public abstract Builder checkinCount(long checkinCount);

        public abstract Builder intervalSeconds(double intervalSeconds);

        public abstract Builder intervalJitter(double intervalJitter);

        public abstract Builder enrichment(String enrichment);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract BeaconEntry build();
    }
}
