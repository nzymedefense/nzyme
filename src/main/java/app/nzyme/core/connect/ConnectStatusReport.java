package app.nzyme.core.connect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class ConnectStatusReport {

    @JsonProperty("version")
    public abstract String version();

    @JsonProperty("local_time")
    public abstract DateTime localTime();

    @JsonProperty("java_vendor")
    public abstract String javaVendor();

    @JsonProperty("java_version")
    public abstract String javaVersion();

    @JsonProperty("os_name")
    public abstract String osName();

    @JsonProperty("os_architecture")
    public abstract String osArchitecture();

    @JsonProperty("os_version")
    public abstract String osVersion();

    @JsonProperty("health_indicators")
    public abstract List<ConnectHealthIndicatorReport> healthIndicators();

    public static ConnectStatusReport create(String version, DateTime localTime, String javaVendor, String javaVersion, String osName, String osArchitecture, String osVersion, List<ConnectHealthIndicatorReport> healthIndicators) {
        return builder()
                .version(version)
                .localTime(localTime)
                .javaVendor(javaVendor)
                .javaVersion(javaVersion)
                .osName(osName)
                .osArchitecture(osArchitecture)
                .osVersion(osVersion)
                .healthIndicators(healthIndicators)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectStatusReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder version(String version);

        public abstract Builder localTime(DateTime localTime);

        public abstract Builder javaVendor(String javaVendor);

        public abstract Builder javaVersion(String javaVersion);

        public abstract Builder osName(String osName);

        public abstract Builder osArchitecture(String osArchitecture);

        public abstract Builder osVersion(String osVersion);

        public abstract Builder healthIndicators(List<ConnectHealthIndicatorReport> healthIndicators);

        public abstract ConnectStatusReport build();
    }
}
