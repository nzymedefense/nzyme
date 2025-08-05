package app.nzyme.core.rest.resources.taps.reports.tables.gnss;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;



@AutoValue
public abstract class GNSSConstellationReport {

    public abstract List<String> fixes();
    @Nullable
    public abstract Double maximumTimeDeviationMs();
    public abstract List<GNSSLatLonReport> positions();
    @Nullable
    public abstract Integer maximumFixSatelliteCount();
    @Nullable
    public abstract Integer minimumFixSatelliteCount();
    public abstract List<Integer> fixSatellites();
    @Nullable
    public abstract Double maximumAltitudeMeters();
    @Nullable
    public abstract Double minimumAltitudeMeters();
    @Nullable
    public abstract Double maximumPdop();
    @Nullable
    public abstract Double minimumPdop();
    public abstract List<GNSSSatelliteInfoReport> satellitesInView();
    @Nullable
    public abstract Integer maximumSatellitesInViewCount();
    @Nullable
    public abstract Integer minimumSatellitesInViewCount();
    public abstract DateTime timestamp();

    @JsonCreator
    public static GNSSConstellationReport create(@JsonProperty("fixes") List<String> fixes,
                                                 @JsonProperty("maximum_time_deviation_ms") Double maximumTimeDeviationMs,
                                                 @JsonProperty("positions") List<GNSSLatLonReport> positions,
                                                 @JsonProperty("maximum_fix_satellite_count") Integer maximumFixSatelliteCount,
                                                 @JsonProperty("minimum_fix_satellite_count") Integer minimumFixSatelliteCount,
                                                 @JsonProperty("fix_satellites") List<Integer> fixSatellites,
                                                 @JsonProperty("maximum_altitude_meters") Double maximumAltitudeMeters,
                                                 @JsonProperty("minimum_altitude_meters") Double minimumAltitudeMeters,
                                                 @JsonProperty("maximum_pdop") Double maximumPdop,
                                                 @JsonProperty("minimum_pdop") Double minimumPdop,
                                                 @JsonProperty("satellites_in_view") List<GNSSSatelliteInfoReport> satellitesInView,
                                                 @JsonProperty("maximum_satellites_in_view_count") Integer maximumSatellitesInViewCount,
                                                 @JsonProperty("minimum_satellites_in_view_count") Integer minimumSatellitesInViewCount,
                                                 @JsonProperty("timestamp") DateTime timestamp) {
        return builder()
                .fixes(fixes)
                .maximumTimeDeviationMs(maximumTimeDeviationMs)
                .positions(positions)
                .maximumFixSatelliteCount(maximumFixSatelliteCount)
                .minimumFixSatelliteCount(minimumFixSatelliteCount)
                .fixSatellites(fixSatellites)
                .maximumAltitudeMeters(maximumAltitudeMeters)
                .minimumAltitudeMeters(minimumAltitudeMeters)
                .maximumPdop(maximumPdop)
                .minimumPdop(minimumPdop)
                .satellitesInView(satellitesInView)
                .maximumSatellitesInViewCount(maximumSatellitesInViewCount)
                .minimumSatellitesInViewCount(minimumSatellitesInViewCount)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSConstellationReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder fixes(List<String> fixes);

        public abstract Builder maximumTimeDeviationMs(Double maximumTimeDeviationMs);

        public abstract Builder positions(List<GNSSLatLonReport> positions);

        public abstract Builder maximumFixSatelliteCount(Integer maximumFixSatelliteCount);

        public abstract Builder minimumFixSatelliteCount(Integer minimumFixSatelliteCount);

        public abstract Builder fixSatellites(List<Integer> fixSatellites);

        public abstract Builder maximumAltitudeMeters(Double maximumAltitudeMeters);

        public abstract Builder minimumAltitudeMeters(Double minimumAltitudeMeters);

        public abstract Builder maximumPdop(Double maximumPdop);

        public abstract Builder minimumPdop(Double minimumPdop);

        public abstract Builder satellitesInView(List<GNSSSatelliteInfoReport> satellitesInView);

        public abstract Builder maximumSatellitesInViewCount(Integer maximumSatellitesInViewCount);

        public abstract Builder minimumSatellitesInViewCount(Integer minimumSatellitesInViewCount);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract GNSSConstellationReport build();
    }
}
