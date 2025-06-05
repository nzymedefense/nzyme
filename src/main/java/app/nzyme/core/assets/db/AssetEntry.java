package app.nzyme.core.assets.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class AssetEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract String mac();
    public abstract String dhcpFingerprint();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();
    public abstract DateTime updatedAt();
    public abstract DateTime createdAt();

    public static AssetEntry create(long id, UUID uuid, UUID organizationId, UUID tenantId, String mac, String dhcpFingerprint, DateTime firstSeen, DateTime lastSeen, DateTime updatedAt, DateTime createdAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .mac(mac)
                .dhcpFingerprint(dhcpFingerprint)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AssetEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder mac(String mac);

        public abstract Builder dhcpFingerprint(String dhcpFingerprint);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract AssetEntry build();
    }
}
