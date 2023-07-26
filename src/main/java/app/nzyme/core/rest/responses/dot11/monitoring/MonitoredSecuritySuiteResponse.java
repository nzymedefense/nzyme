package app.nzyme.core.rest.responses.dot11.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class MonitoredSecuritySuiteResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("suite")
    public abstract String suite();

    public static MonitoredSecuritySuiteResponse create(UUID uuid, String suite) {
        return builder()
                .uuid(uuid)
                .suite(suite)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredSecuritySuiteResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder suite(String suite);

        public abstract MonitoredSecuritySuiteResponse build();
    }
}
