package app.nzyme.core.rest.responses.uav;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UavMapCenterResponse {

    @JsonProperty("zoom")
    public abstract int zoom();

    @JsonProperty("latitude")
    public abstract double latitude();

    @JsonProperty("longitude")
    public abstract double longitude();

    public static UavMapCenterResponse create(int zoom, double latitude, double longitude) {
        return builder()
                .zoom(zoom)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavMapCenterResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder zoom(int zoom);

        public abstract Builder latitude(double latitude);

        public abstract Builder longitude(double longitude);

        public abstract UavMapCenterResponse build();
    }
}
