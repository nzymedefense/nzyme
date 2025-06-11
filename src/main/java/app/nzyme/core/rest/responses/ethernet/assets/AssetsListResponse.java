package app.nzyme.core.rest.responses.ethernet.assets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class AssetsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("assets")
    public abstract List<AssetDetailsResponse> assets();

    public static AssetsListResponse create(long total, List<AssetDetailsResponse> assets) {
        return builder()
                .total(total)
                .assets(assets)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AssetsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder assets(List<AssetDetailsResponse> assets);

        public abstract AssetsListResponse build();
    }

}
