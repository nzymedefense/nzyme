package app.nzyme.core.rest.responses.dot11.monitoring.ssids;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class KnownNetworksListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("networks")
    public abstract List<KnownNetworkDetailsResponse> networks();

    public static KnownNetworksListResponse create(long total, List<KnownNetworkDetailsResponse> networks) {
        return builder()
                .total(total)
                .networks(networks)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_KnownNetworksListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder networks(List<KnownNetworkDetailsResponse> networks);

        public abstract KnownNetworksListResponse build();
    }
}
