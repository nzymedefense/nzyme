package app.nzyme.core.distributed.database;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class NodeEntry {

    public abstract UUID uuid();
    public abstract String name();
    public abstract String transportAddress();

    public abstract String version();
    public abstract DateTime lastSeen();

    public static NodeEntry create(UUID uuid, String name, String transportAddress, String version, DateTime lastSeen) {
        return builder()
                .uuid(uuid)
                .name(name)
                .transportAddress(transportAddress)
                .version(version)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NodeEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract Builder transportAddress(String transportAddress);

        public abstract Builder version(String version);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract NodeEntry build();
    }

}
