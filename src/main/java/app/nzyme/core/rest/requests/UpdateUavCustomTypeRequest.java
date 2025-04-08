package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

@AutoValue
public abstract class UpdateUavCustomTypeRequest {

    @NotBlank
    public abstract String matchType();
    @NotBlank
    public abstract String matchValue();
    @Nullable
    public abstract String defaultClassification();
    @NotBlank
    public abstract String type();
    @NotBlank
    public abstract String name();
    @Nullable
    public abstract String model();

    @JsonCreator
    public static UpdateUavCustomTypeRequest create(@JsonProperty("match_type") String matchType,
                                                    @JsonProperty("match_value") String matchValue,
                                                    @JsonProperty("default_classification") String defaultClassification,
                                                    @JsonProperty("type") String type,
                                                    @JsonProperty("name") String name,
                                                    @JsonProperty("model") String model) {
        return builder()
                .matchType(matchType)
                .matchValue(matchValue)
                .defaultClassification(defaultClassification)
                .type(type)
                .name(name)
                .model(model)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateUavCustomTypeRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder matchType(@NotBlank String matchType);

        public abstract Builder matchValue(@NotBlank String matchValue);

        public abstract Builder defaultClassification(String defaultClassification);

        public abstract Builder type(@NotBlank String type);

        public abstract Builder name(@NotBlank String name);

        public abstract Builder model(String model);

        public abstract UpdateUavCustomTypeRequest build();
    }
}
