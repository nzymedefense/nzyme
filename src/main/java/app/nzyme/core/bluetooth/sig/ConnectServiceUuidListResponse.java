package app.nzyme.core.bluetooth.sig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ConnectServiceUuidListResponse {

    public abstract List<ConnectServiceUuidResponse> serviceUuids();

    @JsonCreator
    public static ConnectServiceUuidListResponse create(@JsonProperty("service_uuids") List<ConnectServiceUuidResponse> serviceUuids) {
        return builder()
                .serviceUuids(serviceUuids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectServiceUuidListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder serviceUuids(List<ConnectServiceUuidResponse> serviceUuids);

        public abstract ConnectServiceUuidListResponse build();
    }
}
