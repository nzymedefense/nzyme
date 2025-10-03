package app.nzyme.core.rest.responses.uav;

import app.nzyme.core.rest.responses.taps.TapHighLevelInformationDetailsResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UavToTapDistanceResponse {

    @JsonProperty("tap")
    public abstract TapHighLevelInformationDetailsResponse tap();

    @JsonProperty("distance_feet")
    public abstract double distanceFeet();

    public static UavToTapDistanceResponse create(TapHighLevelInformationDetailsResponse tap, double distanceFeet) {
        return builder()
                .tap(tap)
                .distanceFeet(distanceFeet)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavToTapDistanceResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tap(TapHighLevelInformationDetailsResponse tap);

        public abstract Builder distanceFeet(double distanceFeet);

        public abstract UavToTapDistanceResponse build();
    }
}
