package app.nzyme.core.context.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.net.InetAddress;
import java.util.UUID;

@AutoValue
public abstract class MacAddressTransparentContextEntry {

    public abstract long id();
    public abstract long contextId();
    public abstract UUID tapUuid();
    public abstract String type();
    @Nullable
    public abstract InetAddress ipAddress();
    @Nullable
    public abstract String hostname();
    public abstract String source();
    public abstract DateTime lastSeen();
    public abstract DateTime createdAt();

    public static MacAddressTransparentContextEntry create(long id, long contextId, UUID tapUuid, String type, InetAddress ipAddress, String hostname, String source, DateTime lastSeen, DateTime createdAt) {
        return builder()
                .id(id)
                .contextId(contextId)
                .tapUuid(tapUuid)
                .type(type)
                .ipAddress(ipAddress)
                .hostname(hostname)
                .source(source)
                .lastSeen(lastSeen)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MacAddressTransparentContextEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder contextId(long contextId);

        public abstract Builder tapUuid(UUID tapUuid);

        public abstract Builder type(String type);

        public abstract Builder ipAddress(InetAddress ipAddress);

        public abstract Builder hostname(String hostname);

        public abstract Builder source(String source);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract MacAddressTransparentContextEntry build();
    }
}
