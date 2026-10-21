package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.List;

@AutoValue
public abstract class BeaconDestinationConnectionsResponse {

    @Nullable
    @JsonProperty("destination_address")
    public abstract String destinationAddress();

    @JsonProperty("connections")
    public abstract List<BeaconDestinationConnectionResponse> connections();

    public static BeaconDestinationConnectionsResponse create(String destinationAddress, List<BeaconDestinationConnectionResponse> connections) {
        return builder()
                .destinationAddress(destinationAddress)
                .connections(connections)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconDestinationConnectionsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder destinationAddress(String destinationAddress);
        public abstract Builder connections(List<BeaconDestinationConnectionResponse> connections);
        public abstract BeaconDestinationConnectionsResponse build();
    }

}
