package app.nzyme.core.rest.resources.taps.reports.tables.uav;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class UavReport {

    public abstract String identifier();
    public abstract List<Integer> rssis();
    public abstract String detectionSource();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();
    @Nullable
    public abstract String uavType();
    public abstract List<UavIdReport> uavIds();
    public abstract List<String> operatorIds();
    public abstract List<String> flightDescriptions();
    public abstract List<UavOperatorLocationReport> operatorLocationReports();
    public abstract List<UavVectorReport> vectorReports();

    @JsonCreator
    public static UavReport create(@JsonProperty("identifier") String identifier,
                                   @JsonProperty("rssis") List<Integer> rssis,
                                   @JsonProperty("detection_source") String detectionSource,
                                   @JsonProperty("first_seen") DateTime firstSeen,
                                   @JsonProperty("last_seen") DateTime lastSeen,
                                   @JsonProperty("uav_type") String uavType,
                                   @JsonProperty("uav_ids") List<UavIdReport> uavIds,
                                   @JsonProperty("operator_ids") List<String> operatorIds,
                                   @JsonProperty("flight_descriptions") List<String> flightDescriptions,
                                   @JsonProperty("operator_location_reports") List<UavOperatorLocationReport> operatorLocationReports,
                                   @JsonProperty("vector_reports") List<UavVectorReport> vectorReports) {
        return builder()
                .identifier(identifier)
                .rssis(rssis)
                .detectionSource(detectionSource)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .uavType(uavType)
                .uavIds(uavIds)
                .operatorIds(operatorIds)
                .flightDescriptions(flightDescriptions)
                .operatorLocationReports(operatorLocationReports)
                .vectorReports(vectorReports)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder identifier(String identifier);

        public abstract Builder rssis(List<Integer> rssis);

        public abstract Builder detectionSource(String detectionSource);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder uavType(String uavType);

        public abstract Builder uavIds(List<UavIdReport> uavIds);

        public abstract Builder operatorIds(List<String> operatorIds);

        public abstract Builder flightDescriptions(List<String> flightDescriptions);

        public abstract Builder operatorLocationReports(List<UavOperatorLocationReport> operatorLocationReports);

        public abstract Builder vectorReports(List<UavVectorReport> vectorReports);

        public abstract UavReport build();
    }
}
