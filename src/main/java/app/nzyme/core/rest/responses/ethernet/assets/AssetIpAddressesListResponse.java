package app.nzyme.core.rest.responses.ethernet.assets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class AssetIpAddressesListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("addresses")
    public abstract List<AssetIpAddressDetailsResponse> addresses();

    public static AssetIpAddressesListResponse create(long total, List<AssetIpAddressDetailsResponse> addresses) {
        return builder()
                .total(total)
                .addresses(addresses)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AssetIpAddressesListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder addresses(List<AssetIpAddressDetailsResponse> addresses);

        public abstract AssetIpAddressesListResponse build();
    }
}
