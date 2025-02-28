package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

@AutoValue
public abstract class CreateUavCustomTypeRequest {

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

    @JsonCreator
    public static CreateUavCustomTypeRequest create(@JsonProperty("match_type") String matchType,
                                                    @JsonProperty("match_value") String matchValue,
                                                    @JsonProperty("default_classification") String defaultClassification,
                                                    @JsonProperty("type") String type,
                                                    @JsonProperty("name") String name) {
        return builder()
                .matchType(matchType)
                .matchValue(matchValue)
                .defaultClassification(defaultClassification)
                .type(type)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateUavCustomTypeRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder matchType(String matchType);

        public abstract Builder matchValue(String matchValue);

        public abstract Builder defaultClassification(String defaultClassification);

        public abstract Builder type(String type);

        public abstract Builder name(String name);

        public abstract CreateUavCustomTypeRequest build();
    }
}
