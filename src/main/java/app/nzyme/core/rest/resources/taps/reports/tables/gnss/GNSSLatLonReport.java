package app.nzyme.core.rest.resources.taps.reports.tables.gnss;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GNSSLatLonReport {

    @JsonProperty("lat")
    public abstract double lat();
    @JsonProperty("lon")
    public abstract double lon();

    @JsonCreator
    public static GNSSLatLonReport create(@JsonProperty("lat") double lat,
                                          @JsonProperty("lon") double lon) {
        return builder()
                .lat(lat)
                .lon(lon)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSLatLonReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder lat(double lat);

        public abstract Builder lon(double lon);

        public abstract GNSSLatLonReport build();
    }
}
