package app.nzyme.core.rest.responses.ethernet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class EthernetMacAddressResponse {

    @JsonProperty("address")
    public abstract String address();

    @JsonProperty("oui")
    @Nullable
    public abstract String oui();

    @JsonProperty("context")
    @Nullable
    public abstract EthernetMacAddressContextResponse context();

    public static EthernetMacAddressResponse create(String address, String oui, EthernetMacAddressContextResponse context) {
        return builder()
                .address(address)
                .oui(oui)
                .context(context)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EthernetMacAddressResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder address(String address);

        public abstract Builder oui(String oui);

        public abstract Builder context(EthernetMacAddressContextResponse context);

        public abstract EthernetMacAddressResponse build();
    }
}
