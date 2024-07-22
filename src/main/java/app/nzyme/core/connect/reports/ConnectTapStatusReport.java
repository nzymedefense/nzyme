package app.nzyme.core.connect.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ConnectTapStatusReport {

    @JsonProperty("version")
    public abstract String version();

    @JsonProperty("uuid")
    public abstract String uuid();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("throughput")
    public abstract long throughput();

    @JsonProperty("local_time")
    public abstract DateTime localTime();

    @JsonProperty("remote_address")
    public abstract String remoteAddress();

    @JsonProperty("cpu_utilization")
    public abstract double cpuUtilization();

    @JsonProperty("memory_utilization")
    public abstract double memoryUtilization();

    @JsonProperty("organization_name")
    public abstract String organizationName();

    @JsonProperty("tenant_name")
    public abstract String tenantName();

    @JsonProperty("log_counts")
    public abstract ConnectTapLogCountReport logCounts();

    @JsonProperty("last_report")
    public abstract DateTime lastReport();

    public static ConnectTapStatusReport create(String version, String uuid, String name, long throughput, DateTime localTime, String remoteAddress, double cpuUtilization, double memoryUtilization, String organizationName, String tenantName, ConnectTapLogCountReport logCounts, DateTime lastReport) {
        return builder()
                .version(version)
                .uuid(uuid)
                .name(name)
                .throughput(throughput)
                .localTime(localTime)
                .remoteAddress(remoteAddress)
                .cpuUtilization(cpuUtilization)
                .memoryUtilization(memoryUtilization)
                .organizationName(organizationName)
                .tenantName(tenantName)
                .logCounts(logCounts)
                .lastReport(lastReport)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectTapStatusReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder version(String version);

        public abstract Builder uuid(String uuid);

        public abstract Builder name(String name);

        public abstract Builder throughput(long throughput);

        public abstract Builder localTime(DateTime localTime);

        public abstract Builder remoteAddress(String remoteAddress);

        public abstract Builder cpuUtilization(double cpuUtilization);

        public abstract Builder memoryUtilization(double memoryUtilization);

        public abstract Builder organizationName(String organizationName);

        public abstract Builder tenantName(String tenantName);

        public abstract Builder logCounts(ConnectTapLogCountReport logCounts);

        public abstract Builder lastReport(DateTime lastReport);

        public abstract ConnectTapStatusReport build();
    }
}
