package app.nzyme.core.rest.responses.floorplans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FloorPlanResponse {

    @JsonProperty("image_base64")
    public abstract String imageBase64();

    @JsonProperty("width_pixels")
    public abstract int widthPixels();

    @JsonProperty("length_pixels")
    public abstract int lengthPixels();

    @JsonProperty("width_meters")
    public abstract int widthMeters();

    @JsonProperty("length_meters")
    public abstract int lengthMeters();

    public static FloorPlanResponse create(String imageBase64, int widthPixels, int lengthPixels, int widthMeters, int lengthMeters) {
        return builder()
                .imageBase64(imageBase64)
                .widthPixels(widthPixels)
                .lengthPixels(lengthPixels)
                .widthMeters(widthMeters)
                .lengthMeters(lengthMeters)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_FloorPlanResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder imageBase64(String imageBase64);

        public abstract Builder widthPixels(int widthPixels);

        public abstract Builder lengthPixels(int lengthPixels);

        public abstract Builder widthMeters(int widthMeters);

        public abstract Builder lengthMeters(int lengthMeters);

        public abstract FloorPlanResponse build();
    }
}
