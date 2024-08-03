package app.nzyme.core.util.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FilterFrontendParameter {

    @JsonProperty("field")
    public abstract String field();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("operator")
    public abstract String operator();

    @JsonProperty("sign")
    public abstract String sign();

    @JsonProperty("value")
    public abstract String value();

    public static FilterFrontendParameter create(String field, String name, String operator, String sign, String value) {
        return builder()
                .field(field)
                .name(name)
                .operator(operator)
                .sign(sign)
                .value(value)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_FilterFrontendParameter.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder field(String field);

        public abstract Builder name(String name);

        public abstract Builder operator(String operator);

        public abstract Builder sign(String sign);

        public abstract Builder value(String value);

        public abstract FilterFrontendParameter build();
    }
}
