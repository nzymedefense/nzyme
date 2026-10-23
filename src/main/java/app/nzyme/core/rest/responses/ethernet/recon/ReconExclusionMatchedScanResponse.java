package app.nzyme.core.rest.responses.ethernet.recon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class ReconExclusionMatchedScanResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("type")
    public abstract String type();

    @JsonProperty("source")
    public abstract String source();

    @JsonProperty("target")
    public abstract String target();

    public static ReconExclusionMatchedScanResponse create(UUID uuid, String type, String source, String target) {
        return builder()
                .uuid(uuid)
                .type(type)
                .source(source)
                .target(target)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconExclusionMatchedScanResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);
        public abstract Builder type(String type);
        public abstract Builder source(String source);
        public abstract Builder target(String target);
        public abstract ReconExclusionMatchedScanResponse build();
    }
}
