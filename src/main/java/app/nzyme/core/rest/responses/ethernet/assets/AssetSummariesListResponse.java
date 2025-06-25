package app.nzyme.core.rest.responses.ethernet.assets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class AssetSummariesListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("assets")
    public abstract List<AssetSummaryResponse> assets();

    public static AssetSummariesListResponse create(long total, List<AssetSummaryResponse> assets) {
        return builder()
                .total(total)
                .assets(assets)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AssetSummariesListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder assets(List<AssetSummaryResponse> assets);

        public abstract AssetSummariesListResponse build();
    }
}
