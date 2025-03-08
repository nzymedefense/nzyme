package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;

@AutoValue
public abstract class CreateTapRequest {

    @NotEmpty
    public abstract String name();

    @NotEmpty
    public abstract String description();

    @Nullable
    public abstract Double latitude();

    @Nullable
    public abstract Double longitude();

    @JsonCreator
    public static CreateTapRequest create(@JsonProperty("name") String name,
                                          @JsonProperty("description") String description,
                                          @JsonProperty("latitude") Double latitude,
                                          @JsonProperty("longitude") Double longitude) {
        return builder()
                .name(name)
                .description(description)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateTapRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder latitude(Double latitude);

        public abstract Builder longitude(Double longitude);

        public abstract CreateTapRequest build();
    }

}
