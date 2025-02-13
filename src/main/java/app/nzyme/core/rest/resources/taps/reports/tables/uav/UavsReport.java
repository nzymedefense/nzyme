package app.nzyme.core.rest.resources.taps.reports.tables.uav;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class UavsReport {

    public abstract List<UavReport> uavs();

    @JsonCreator
    public static UavsReport create(@JsonProperty("uavs") List<UavReport> uavs) {
        return builder()
                .uavs(uavs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uavs(List<UavReport> uavs);

        public abstract UavsReport build();
    }
}
