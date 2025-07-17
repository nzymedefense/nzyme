package app.nzyme.core.rest.responses.ethernet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class InternalAddressResponse {

    @JsonProperty("l4_type")
    public abstract L4AddressTypeResponse l4Type();

    @Nullable
    @JsonProperty("mac")
    public abstract EthernetMacAddressResponse mac();

    @JsonProperty("address")
    public abstract String address();

    @JsonProperty("port")
    @Nullable
    public abstract Integer port();

    @Nullable
    @JsonProperty("context")
    public abstract L4AddressContextResponse context();

    public static InternalAddressResponse create(L4AddressTypeResponse l4Type, EthernetMacAddressResponse mac, String address, Integer port, L4AddressContextResponse context) {
        return builder()
                .l4Type(l4Type)
                .mac(mac)
                .address(address)
                .port(port)
                .context(context)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_InternalAddressResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder l4Type(L4AddressTypeResponse l4Type);

        public abstract Builder mac(EthernetMacAddressResponse mac);

        public abstract Builder address(String address);

        public abstract Builder port(Integer port);

        public abstract Builder context(L4AddressContextResponse context);

        public abstract InternalAddressResponse build();
    }
}
