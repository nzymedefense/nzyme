package app.nzyme.core.rest.resources.taps.reports.tables.ssh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class SshVersionReport {

    public abstract String version();
    public abstract String software();
    @Nullable
    public abstract String comments();

    @JsonCreator
    public static SshVersionReport create(@JsonProperty("version") String version,
                                          @JsonProperty("software") String software,
                                          @JsonProperty("comments") String comments) {
        return builder()
                .version(version)
                .software(software)
                .comments(comments)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SshVersionReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder version(String version);

        public abstract Builder software(String software);

        public abstract Builder comments(String comments);

        public abstract SshVersionReport build();
    }
}
