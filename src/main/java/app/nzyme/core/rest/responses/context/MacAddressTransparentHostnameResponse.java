package app.nzyme.core.rest.responses.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class MacAddressTransparentHostnameResponse {

    @JsonProperty("hostname")
    public abstract String hostname();

    @JsonProperty("source")
    public abstract String source();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static MacAddressTransparentHostnameResponse create(String hostname, String source, DateTime lastSeen, DateTime createdAt) {
        return builder()
                .hostname(hostname)
                .source(source)
                .lastSeen(lastSeen)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MacAddressTransparentHostnameResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder hostname(String hostname);

        public abstract Builder source(String source);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract MacAddressTransparentHostnameResponse build();
    }
}
