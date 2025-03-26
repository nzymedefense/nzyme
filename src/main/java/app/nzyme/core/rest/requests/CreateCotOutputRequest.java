package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@AutoValue
public abstract class CreateCotOutputRequest {

    @NotBlank
    public abstract String name();
    @Nullable
    public abstract String description();
    @NotBlank
    public abstract String leafTypeTap();
    @NotBlank
    public abstract String address();
    @Min(1)
    @Max(65535)
    public abstract int port();

    @JsonCreator
    public static CreateCotOutputRequest create(@JsonProperty("name") String name,
                                                @JsonProperty("description") String description,
                                                @JsonProperty("leaf_type_tap") String leafTypeTap,
                                                @JsonProperty("address") String address,
                                                @JsonProperty("port") int port) {
        return builder()
                .name(name)
                .description(description)
                .leafTypeTap(leafTypeTap)
                .address(address)
                .port(port)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateCotOutputRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(@NotBlank String name);

        public abstract Builder description(@NotBlank String description);

        public abstract Builder leafTypeTap(@NotBlank String leafTypeTap);

        public abstract Builder address(@NotBlank String address);

        public abstract Builder port(@Min(1) @Max(65535) int port);

        public abstract CreateCotOutputRequest build();
    }
}
