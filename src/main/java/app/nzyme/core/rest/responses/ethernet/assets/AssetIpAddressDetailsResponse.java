package app.nzyme.core.rest.responses.ethernet.assets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class AssetIpAddressDetailsResponse {

    @JsonProperty("address")
    public abstract String address();
    @JsonProperty("source")
    public abstract String source();
    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();
    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static AssetIpAddressDetailsResponse create(String address, String source, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .address(address)
                .source(source)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AssetIpAddressDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder address(String address);

        public abstract Builder source(String source);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract AssetIpAddressDetailsResponse build();
    }
}
