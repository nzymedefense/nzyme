package app.nzyme.core.rest.responses.bluetooth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class BluetoothMacAddressResponse {

    @JsonProperty("address")
    public abstract String address();

    @JsonProperty("oui")
    @Nullable
    public abstract String oui();

    @JsonProperty("is_randomized")
    @Nullable
    public abstract Boolean isRandomized();

    @JsonProperty("context")
    @Nullable
    public abstract BluetoothMacAddressContextResponse context();

    public static BluetoothMacAddressResponse create(String address, String oui, Boolean isRandomized, BluetoothMacAddressContextResponse context) {
        return builder()
                .address(address)
                .oui(oui)
                .isRandomized(isRandomized)
                .context(context)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BluetoothMacAddressResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder address(String address);

        public abstract Builder oui(String oui);

        public abstract Builder isRandomized(Boolean isRandomized);

        public abstract Builder context(BluetoothMacAddressContextResponse context);

        public abstract BluetoothMacAddressResponse build();
    }

}
