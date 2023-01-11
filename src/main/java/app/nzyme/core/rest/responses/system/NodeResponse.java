package app.nzyme.core.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class NodeResponse {

    @JsonProperty("uuid")
    public abstract String uuid();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("transport_address")
    public abstract String transportAddress();

    @JsonProperty("version")
    public abstract String version();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static NodeResponse create(String uuid, String name, String transportAddress, String version, DateTime lastSeen) {
        return builder()
                .uuid(uuid)
                .name(name)
                .transportAddress(transportAddress)
                .version(version)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NodeResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(String uuid);

        public abstract Builder name(String name);

        public abstract Builder transportAddress(String transportAddress);

        public abstract Builder version(String version);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract NodeResponse build();
    }

}
