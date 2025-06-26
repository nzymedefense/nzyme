package app.nzyme.core.rest.responses.ethernet.assets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class AssetHostnameDetailsResponse {

    @JsonProperty("id")
    public abstract UUID id();
    @JsonProperty("hostname")
    public abstract String hostname();
    @JsonProperty("source")
    public abstract String source();
    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();
    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static AssetHostnameDetailsResponse create(UUID id, String hostname, String source, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .id(id)
                .hostname(hostname)
                .source(source)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AssetHostnameDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder hostname(String hostname);

        public abstract Builder source(String source);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract AssetHostnameDetailsResponse build();
    }
}
