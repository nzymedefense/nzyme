package app.nzyme.core.rest.responses.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class MacAddressContextListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("mac_addresses")
    public abstract List<MacAddressContextDetailsResponse> macAddresses();

    public static MacAddressContextListResponse create(long total, List<MacAddressContextDetailsResponse> macAddresses) {
        return builder()
                .total(total)
                .macAddresses(macAddresses)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MacAddressContextListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder macAddresses(List<MacAddressContextDetailsResponse> macAddresses);

        public abstract MacAddressContextListResponse build();
    }
}
