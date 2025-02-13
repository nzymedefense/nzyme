package app.nzyme.core.rest.resources.taps.reports.tables.uav;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UavIdReport {

    public abstract String idType();
    public abstract String id();

    @JsonCreator
    public static UavIdReport create(@JsonProperty("id_type") String idType,
                                     @JsonProperty("id") String id) {
        return builder()
                .idType(idType)
                .id(id)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavIdReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder idType(String idType);

        public abstract Builder id(String id);

        public abstract UavIdReport build();
    }
}
