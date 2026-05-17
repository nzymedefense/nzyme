package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

@AutoValue
public abstract class UpdateTapRequest {

    @NotEmpty
    public abstract String name();

    @NotEmpty
    public abstract String description();

    @Nullable
    public abstract UUID location();

    @Nullable
    @Min(-90) @Max(90)
    public abstract Double latitude();

    @Nullable
    @Min(-180) @Max(180)
    public abstract Double longitude();

    @JsonCreator
    public static UpdateTapRequest create(@JsonProperty("name") String name,
                                          @JsonProperty("description") String description,
                                          @JsonProperty("location") UUID location,
                                          @JsonProperty("latitude") Double latitude,
                                          @JsonProperty("longitude") Double longitude) {
        return builder()
                .name(name)
                .description(description)
                .location(location)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateTapRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder location(UUID location);

        public abstract Builder latitude(Double latitude);

        public abstract Builder longitude(Double longitude);

        public abstract UpdateTapRequest build();
    }

}
