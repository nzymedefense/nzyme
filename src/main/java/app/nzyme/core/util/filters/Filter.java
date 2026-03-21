package app.nzyme.core.util.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Filter {

    @JsonProperty("field")
    public abstract String field();

    @JsonProperty("operator")
    public abstract FilterOperator operator();

    @JsonProperty("value")
    public abstract Object value();

    @JsonProperty("untransformed_value")
    public abstract Object untransformedValue();

    public static Filter create(String field, FilterOperator operator, Object value, Object untransformedValue) {
        return builder()
                .field(field)
                .operator(operator)
                .value(value)
                .untransformedValue(untransformedValue)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Filter.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder field(String field);

        public abstract Builder operator(FilterOperator operator);

        public abstract Builder value(Object value);

        public abstract Builder untransformedValue(Object untransformedValue);

        public abstract Filter build();
    }
}
