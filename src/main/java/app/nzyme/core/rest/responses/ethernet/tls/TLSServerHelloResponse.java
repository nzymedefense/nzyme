package app.nzyme.core.rest.responses.ethernet.tls;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.List;

@AutoValue
public abstract class TLSServerHelloResponse {

    @Nullable
    @JsonProperty("record_version")
    public abstract String recordVersion();

    @Nullable
    @JsonProperty("legacy_version")
    public abstract String legacyVersion();

    @JsonProperty("compression_method")
    public abstract int compressionMethod();

    @JsonProperty("extensions")
    public abstract List<Integer> extensions();

    public static TLSServerHelloResponse create(String recordVersion, String legacyVersion, int compressionMethod, List<Integer> extensions) {
        return builder()
                .recordVersion(recordVersion)
                .legacyVersion(legacyVersion)
                .compressionMethod(compressionMethod)
                .extensions(extensions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSServerHelloResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder recordVersion(String recordVersion);
        public abstract Builder legacyVersion(String legacyVersion);
        public abstract Builder compressionMethod(int compressionMethod);
        public abstract Builder extensions(List<Integer> extensions);
        public abstract TLSServerHelloResponse build();
    }
}
