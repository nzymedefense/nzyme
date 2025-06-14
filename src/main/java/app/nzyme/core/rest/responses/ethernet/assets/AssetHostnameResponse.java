package app.nzyme.core.rest.responses.ethernet.assets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class AssetHostnameResponse {

    @JsonProperty("hostname")
    public abstract String hostname();
    @JsonProperty("source")
    public abstract String source();
    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static AssetHostnameResponse create(String hostname, String source, DateTime lastSeen) {
        return builder()
                .hostname(hostname)
                .source(source)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AssetHostnameResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder hostname(String hostname);

        public abstract Builder source(String source);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract AssetHostnameResponse build();
    }
}
