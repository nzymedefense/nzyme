package app.nzyme.core.rest.parameters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class FiltersParameter {

    public abstract String field();
    public abstract String operator();
    public abstract String value();
    @Nullable
    public abstract String transformedValue();

    @JsonCreator
    public static FiltersParameter create(@JsonProperty("field") String field,
                                          @JsonProperty("operator") String operator,
                                          @JsonProperty("value") String value,
                                          @JsonProperty("transformed_value") String transformedValue) {
        return builder()
                .field(field)
                .operator(operator)
                .value(value)
                .transformedValue(transformedValue)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_FiltersParameter.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder field(String field);

        public abstract Builder operator(String operator);

        public abstract Builder value(String value);

        public abstract Builder transformedValue(String transformedValue);

        public abstract FiltersParameter build();
    }
}
