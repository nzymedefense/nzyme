package app.nzyme.core.rest.responses.ethernet.assets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class AssetIpAddressResponse {

    @JsonProperty("address")
    public abstract String address();
    @JsonProperty("source")
    public abstract String source();
    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static AssetIpAddressResponse create(String address, String source, DateTime lastSeen) {
        return builder()
                .address(address)
                .source(source)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AssetIpAddressResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder address(String address);

        public abstract Builder source(String source);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract AssetIpAddressResponse build();
    }
}
