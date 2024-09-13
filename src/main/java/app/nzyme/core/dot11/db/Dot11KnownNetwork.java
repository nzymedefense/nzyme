package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class Dot11KnownNetwork {

    public abstract long id();
    public abstract UUID uuid();
    public abstract String ssid();
    public abstract boolean isApproved();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();

    public static Dot11KnownNetwork create(long id, UUID uuid, String ssid, boolean isApproved, UUID organizationId, UUID tenantId, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .id(id)
                .uuid(uuid)
                .ssid(ssid)
                .isApproved(isApproved)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11KnownNetwork.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder ssid(String ssid);

        public abstract Builder isApproved(boolean isApproved);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Dot11KnownNetwork build();
    }
}
