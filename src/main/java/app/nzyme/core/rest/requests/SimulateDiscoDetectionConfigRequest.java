package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class SimulateDiscoDetectionConfigRequest {

    @NotNull
    public abstract UUID monitoredNetworkId();

    @NotNull
    public abstract UUID tapId();

    @NotEmpty
    public abstract String methodType();

    @NotNull
    public abstract Map<String, Object> configuration();

    @JsonCreator
    public static SimulateDiscoDetectionConfigRequest create(@JsonProperty("monitored_network_id") UUID monitoredNetworkId,
                                                             @JsonProperty("tap_id") UUID tapId,
                                                             @JsonProperty("method_type") String methodType,
                                                             @JsonProperty("configuration") Map<String, Object> configuration) {
        return builder()
                .monitoredNetworkId(monitoredNetworkId)
                .tapId(tapId)
                .methodType(methodType)
                .configuration(configuration)
                .build();
    }

    public static SimulateDiscoDetectionConfigRequest.Builder builder() {
        return new AutoValue_SimulateDiscoDetectionConfigRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract SimulateDiscoDetectionConfigRequest.Builder monitoredNetworkId(UUID monitoredNetworkId);

        public abstract SimulateDiscoDetectionConfigRequest.Builder tapId(UUID tapId);

        public abstract SimulateDiscoDetectionConfigRequest.Builder methodType(String methodType);

        public abstract SimulateDiscoDetectionConfigRequest.Builder configuration(Map<String, Object> configuration);

        public abstract SimulateDiscoDetectionConfigRequest build();
    }

}
