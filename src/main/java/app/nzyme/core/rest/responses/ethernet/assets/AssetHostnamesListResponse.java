package app.nzyme.core.rest.responses.ethernet.assets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class AssetHostnamesListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("hostnames")
    public abstract List<AssetHostnameDetailsResponse> hostnames();

    public static AssetHostnamesListResponse create(long total, List<AssetHostnameDetailsResponse> hostnames) {
        return builder()
                .total(total)
                .hostnames(hostnames)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AssetHostnamesListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder hostnames(List<AssetHostnameDetailsResponse> hostnames);

        public abstract AssetHostnamesListResponse build();
    }
}
