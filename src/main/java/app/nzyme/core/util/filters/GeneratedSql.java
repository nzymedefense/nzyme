package app.nzyme.core.util.filters;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GeneratedSql {

    public abstract String where();
    public abstract String having();

    public static GeneratedSql create(String where, String having) {
        return builder()
                .where(where)
                .having(having)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GeneratedSql.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder where(String where);

        public abstract Builder having(String having);

        public abstract GeneratedSql build();
    }
}
