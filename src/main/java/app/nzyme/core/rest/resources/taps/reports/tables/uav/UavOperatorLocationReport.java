package app.nzyme.core.rest.resources.taps.reports.tables.uav;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class UavOperatorLocationReport {

    public abstract DateTime timestamp();
    public abstract List<String> locationTypes();
    @Nullable
    public abstract Double latitude();
    @Nullable
    public abstract Double longitude();
    @Nullable
    public abstract Double altitude();

    @JsonCreator
    public static UavOperatorLocationReport create(@JsonProperty("timestamp") DateTime timestamp,
                                                   @JsonProperty("location_types") List<String> locationTypes,
                                                   @JsonProperty("latitude") Double latitude,
                                                   @JsonProperty("longitude") Double longitude,
                                                   @JsonProperty("altitude") Double altitude) {
        return builder()
                .timestamp(timestamp)
                .locationTypes(locationTypes)
                .latitude(latitude)
                .longitude(longitude)
                .altitude(altitude)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavOperatorLocationReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder locationTypes(List<String> locationTypes);

        public abstract Builder latitude(Double latitude);

        public abstract Builder longitude(Double longitude);

        public abstract Builder altitude(Double altitude);

        public abstract UavOperatorLocationReport build();
    }
}
