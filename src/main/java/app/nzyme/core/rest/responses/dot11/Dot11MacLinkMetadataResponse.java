package app.nzyme.core.rest.responses.dot11;

import app.nzyme.core.dot11.Dot11MacAddressType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Dot11MacLinkMetadataResponse {

    @JsonProperty("type")
    public abstract Dot11MacAddressType type();

    public static Dot11MacLinkMetadataResponse create(Dot11MacAddressType type) {
        return builder()
                .type(type)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11MacLinkMetadataResponse.Builder();
    }
    
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(Dot11MacAddressType type);

        public abstract Dot11MacLinkMetadataResponse build();
    }
}
