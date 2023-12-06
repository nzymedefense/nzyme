package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class Dot11MacAddressResponse {

    @JsonProperty("address")
    public abstract String address();

    @JsonProperty("oui")
    @Nullable
    public abstract String oui();

    @JsonProperty("context")
    @Nullable
    public abstract Dot11MacAddressContextResponse context();

    public static Dot11MacAddressResponse create(String address, String oui, Dot11MacAddressContextResponse context) {
        return builder()
                .address(address)
                .oui(oui)
                .context(context)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11MacAddressResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder address(String address);

        public abstract Builder oui(String oui);

        public abstract Builder context(Dot11MacAddressContextResponse context);

        public abstract Dot11MacAddressResponse build();
    }
}
