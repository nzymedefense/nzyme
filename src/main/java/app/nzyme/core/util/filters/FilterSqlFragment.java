package app.nzyme.core.util.filters;

import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class FilterSqlFragment {

    public abstract String sql();
    public abstract Map<String, Object> bindings();

    public static FilterSqlFragment create(String sql, Map<String, Object> bindings) {
        return builder()
                .sql(sql)
                .bindings(bindings)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_FilterSqlFragment.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sql(String sql);

        public abstract Builder bindings(Map<String, Object> bindings);

        public abstract FilterSqlFragment build();
    }
}
