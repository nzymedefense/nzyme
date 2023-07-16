package app.nzyme.core.rest.responses.dot11.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ClientListResponse {

    @JsonProperty("connected")
    public abstract ConnectedClientListResponse connected();

    @JsonProperty("disconnected")
    public abstract DisconnectedClientListResponse disconnected();

    public static ClientListResponse create(ConnectedClientListResponse connected, DisconnectedClientListResponse disconnected) {
        return builder()
                .connected(connected)
                .disconnected(disconnected)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder connected(ConnectedClientListResponse connected);

        public abstract Builder disconnected(DisconnectedClientListResponse disconnected);

        public abstract ClientListResponse build();
    }

}
