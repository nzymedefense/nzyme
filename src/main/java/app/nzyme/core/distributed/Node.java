package app.nzyme.core.distributed;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.net.URI;
import java.util.UUID;

@AutoValue
public abstract class Node {

    public abstract UUID uuid();
    public abstract String name();
    public abstract URI transportAddress();

    public abstract String version();
    public abstract DateTime lastSeen();

    public static Node create(UUID uuid, String name, URI transportAddress, String version, DateTime lastSeen) {
        return builder()
                .uuid(uuid)
                .name(name)
                .transportAddress(transportAddress)
                .version(version)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Node.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract Builder transportAddress(URI transportAddress);

        public abstract Builder version(String version);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Node build();
    }

}
