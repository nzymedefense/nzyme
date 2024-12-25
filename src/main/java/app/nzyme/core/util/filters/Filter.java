package app.nzyme.core.util.filters;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Filter {

    public abstract String field();
    public abstract FilterOperator operator();
    public abstract Object value();
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
