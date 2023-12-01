package app.nzyme.core.rest.responses.dot11;

import app.nzyme.core.dot11.Dot11MacAddressType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Dot11MacLinkMetadataResponse {

    @JsonProperty("type")
    public abstract Dot11MacAddressType type();

    @JsonProperty("mac")
    public abstract Dot11MacAddressResponse mac();

    public static Dot11MacLinkMetadataResponse create(Dot11MacAddressType type, Dot11MacAddressResponse mac) {
        return builder()
                .type(type)
                .mac(mac)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11MacLinkMetadataResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(Dot11MacAddressType type);

        public abstract Builder mac(Dot11MacAddressResponse mac);

        public abstract Dot11MacLinkMetadataResponse build();
    }
}
