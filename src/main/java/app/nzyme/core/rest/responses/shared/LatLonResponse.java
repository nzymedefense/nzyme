package app.nzyme.core.rest.responses.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class LatLonResponse {

    @JsonProperty("lat")
    public abstract double lat();

    @JsonProperty("lon")
    public abstract double lon();

    public static LatLonResponse create(double lat, double lon) {
        return builder()
                .lat(lat)
                .lon(lon)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_LatLonResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder lat(double lat);

        public abstract Builder lon(double lon);

        public abstract LatLonResponse build();
    }
}
