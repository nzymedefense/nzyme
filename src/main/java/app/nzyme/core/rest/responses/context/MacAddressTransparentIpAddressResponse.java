package app.nzyme.core.rest.responses.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class MacAddressTransparentIpAddressResponse {

    @JsonProperty("ip_address")
    public abstract String ipAddress();

    @JsonProperty("source")
    public abstract String source();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static MacAddressTransparentIpAddressResponse create(String ipAddress, String source, DateTime updatedAt, DateTime createdAt) {
        return builder()
                .ipAddress(ipAddress)
                .source(source)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MacAddressTransparentIpAddressResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ipAddress(String ipAddress);

        public abstract Builder source(String source);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract MacAddressTransparentIpAddressResponse build();
    }
}
