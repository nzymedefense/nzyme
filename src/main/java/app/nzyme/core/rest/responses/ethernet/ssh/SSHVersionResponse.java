package app.nzyme.core.rest.responses.ethernet.ssh;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class SSHVersionResponse {

    @JsonProperty("version")
    public abstract String version();

    @JsonProperty("software")
    public abstract String software();

    @Nullable
    @JsonProperty("comments")
    public abstract String comments();

    public static SSHVersionResponse create(String version, String software, String comments) {
        return builder()
                .version(version)
                .software(software)
                .comments(comments)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSHVersionResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder version(String version);

        public abstract Builder software(String software);

        public abstract Builder comments(String comments);

        public abstract SSHVersionResponse build();
    }
}
