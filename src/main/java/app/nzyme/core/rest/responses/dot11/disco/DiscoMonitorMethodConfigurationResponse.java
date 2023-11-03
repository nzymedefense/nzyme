package app.nzyme.core.rest.responses.dot11.disco;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.Map;

@AutoValue
public abstract class DiscoMonitorMethodConfigurationResponse {

    @JsonProperty("method_type")
    public abstract String methodType();

    @JsonProperty("configuration")
    public abstract Map<String, Object> configuration();

    public static DiscoMonitorMethodConfigurationResponse create(String methodType, Map<String, Object> configuration) {
        return builder()
                .methodType(methodType)
                .configuration(configuration)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DiscoMonitorMethodConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder methodType(String methodType);

        public abstract Builder configuration(Map<String, Object> configuration);

        public abstract DiscoMonitorMethodConfigurationResponse build();
    }
}
