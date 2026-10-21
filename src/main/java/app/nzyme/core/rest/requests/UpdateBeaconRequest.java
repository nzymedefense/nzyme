package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Body for renaming a beacon or changing its description.")
@AutoValue
public abstract class UpdateBeaconRequest {

    @NotEmpty
    public abstract String name();

    @Nullable
    public abstract String description();

    @JsonCreator
    public static UpdateBeaconRequest create(@Schema(description = "New name of the beacon. Must be unique within the tenant.", requiredMode = Schema.RequiredMode.REQUIRED)
                                             @JsonProperty("name") String name,
                                             @Schema(description = "Description or operator notes. Pass an empty string or omit to clear.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                             @JsonProperty("description") String description) {
        return builder()
                .name(name)
                .description(description)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateBeaconRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract UpdateBeaconRequest build();
    }
}
