package app.nzyme.core.rest.responses.gnss;

import app.nzyme.core.rest.responses.shared.LatLonResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class GNSSConstellationCoordinatesResponse {

    @JsonProperty("coordinates")
    public abstract List<LatLonResponse> coordinates();
    @JsonProperty("tap_locations")
    public abstract List<GNSSTapLocationResponse> tapLocations();

    public static GNSSConstellationCoordinatesResponse create(List<LatLonResponse> coordinates, List<GNSSTapLocationResponse> tapLocations) {
        return builder()
                .coordinates(coordinates)
                .tapLocations(tapLocations)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSConstellationCoordinatesResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder coordinates(List<LatLonResponse> coordinates);

        public abstract Builder tapLocations(List<GNSSTapLocationResponse> tapLocations);

        public abstract GNSSConstellationCoordinatesResponse build();
    }
}
