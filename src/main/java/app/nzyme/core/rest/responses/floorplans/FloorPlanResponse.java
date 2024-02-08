package app.nzyme.core.rest.responses.floorplans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FloorPlanResponse {

    @JsonProperty("image_base64")
    public abstract String imageBase64();

    @JsonProperty("width")
    public abstract int width();

    @JsonProperty("height")
    public abstract int height();

    public static FloorPlanResponse create(String imageBase64, int width, int height) {
        return builder()
                .imageBase64(imageBase64)
                .width(width)
                .height(height)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_FloorPlanResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder imageBase64(String imageBase64);

        public abstract Builder width(int width);

        public abstract Builder height(int height);

        public abstract FloorPlanResponse build();
    }
}
