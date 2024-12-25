package app.nzyme.core.util.filters;

import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class FilterSqlFragment {

    public abstract String whereSql();
    public abstract String havingSql();
    public abstract Map<String, Object> bindings();

    public static FilterSqlFragment create(String whereSql, String havingSql, Map<String, Object> bindings) {
        return builder()
                .whereSql(whereSql)
                .havingSql(havingSql)
                .bindings(bindings)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_FilterSqlFragment.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder whereSql(String whereSql);

        public abstract Builder havingSql(String havingSql);

        public abstract Builder bindings(Map<String, Object> bindings);

        public abstract FilterSqlFragment build();
    }
}
