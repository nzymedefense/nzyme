package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class Dot11KnownClient {

    public abstract long id();
    public abstract UUID uuid();
    public abstract String mac();
    public abstract boolean isApproved();
    public abstract boolean isIgnored();
    public abstract Long monitoredNetworkId();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();

    public static Dot11KnownClient create(long id, UUID uuid, String mac, boolean isApproved, boolean isIgnored, Long monitoredNetworkId, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .id(id)
                .uuid(uuid)
                .mac(mac)
                .isApproved(isApproved)
                .isIgnored(isIgnored)
                .monitoredNetworkId(monitoredNetworkId)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11KnownClient.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder mac(String mac);

        public abstract Builder isApproved(boolean isApproved);

        public abstract Builder isIgnored(boolean isIgnored);

        public abstract Builder monitoredNetworkId(Long monitoredNetworkId);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Dot11KnownClient build();
    }
}
