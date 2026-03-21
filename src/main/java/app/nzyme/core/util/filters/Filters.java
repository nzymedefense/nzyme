package app.nzyme.core.util.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class Filters {

    @JsonProperty("filters")
    public abstract Map<String, List<Filter>> filters();

    public static Filters create(Map<String, List<Filter>> filters) {
        return builder()
                .filters(filters)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Filters.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder filters(Map<String, List<Filter>> filters);

        public abstract Filters build();
    }
}
