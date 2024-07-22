package app.nzyme.core.connect.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class ConnectStatusReport {

    @JsonProperty("version")
    public abstract String version();

    @JsonProperty("node_id")
    public abstract String nodeId();

    @JsonProperty("node_name")
    public abstract String nodeName();

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

    @JsonProperty("throughput")
    public abstract List<ConnectThroughputReport> throughput();

    @JsonProperty("log_counts")
    public abstract ConnectNodeLogCountReport logCounts();

    @JsonProperty("cpu_utilization")
    public abstract double cpuUtilization();

    @JsonProperty("memory_utilization")
    public abstract double memoryUtilization();

    @JsonProperty("heap_utilization")
    public abstract double heapUtilization();

    public static ConnectStatusReport create(String version, String nodeId, String nodeName, DateTime localTime, String javaVendor, String javaVersion, String osName, String osArchitecture, String osVersion, List<ConnectHealthIndicatorReport> healthIndicators, List<ConnectThroughputReport> throughput, ConnectNodeLogCountReport logCounts, double cpuUtilization, double memoryUtilization, double heapUtilization) {
        return builder()
                .version(version)
                .nodeId(nodeId)
                .nodeName(nodeName)
                .localTime(localTime)
                .javaVendor(javaVendor)
                .javaVersion(javaVersion)
                .osName(osName)
                .osArchitecture(osArchitecture)
                .osVersion(osVersion)
                .healthIndicators(healthIndicators)
                .throughput(throughput)
                .logCounts(logCounts)
                .cpuUtilization(cpuUtilization)
                .memoryUtilization(memoryUtilization)
                .heapUtilization(heapUtilization)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectStatusReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder version(String version);

        public abstract Builder nodeId(String nodeId);

        public abstract Builder nodeName(String nodeName);

        public abstract Builder localTime(DateTime localTime);

        public abstract Builder javaVendor(String javaVendor);

        public abstract Builder javaVersion(String javaVersion);

        public abstract Builder osName(String osName);

        public abstract Builder osArchitecture(String osArchitecture);

        public abstract Builder osVersion(String osVersion);

        public abstract Builder healthIndicators(List<ConnectHealthIndicatorReport> healthIndicators);

        public abstract Builder throughput(List<ConnectThroughputReport> throughput);

        public abstract Builder logCounts(ConnectNodeLogCountReport logCounts);

        public abstract Builder cpuUtilization(double cpuUtilization);

        public abstract Builder memoryUtilization(double memoryUtilization);

        public abstract Builder heapUtilization(double heapUtilization);

        public abstract ConnectStatusReport build();
    }
}
