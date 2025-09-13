package app.nzyme.core.connect.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class ConnectTapStatusReport {

    @JsonProperty("version")
    @Nullable
    public abstract String version();

    @JsonProperty("uuid")
    public abstract String uuid();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("throughput")
    @Nullable
    public abstract Long throughput();

    @JsonProperty("local_time")
    @Nullable
    public abstract DateTime localTime();

    @JsonProperty("remote_address")
    @Nullable
    public abstract String remoteAddress();

    @JsonProperty("cpu_utilization")
    @Nullable
    public abstract Double cpuUtilization();

    @JsonProperty("memory_utilization")
    @Nullable
    public abstract Double memoryUtilization();

    @JsonProperty("organization_name")
    public abstract String organizationName();

    @JsonProperty("tenant_name")
    public abstract String tenantName();

    @JsonProperty("log_counts")
    public abstract ConnectTapLogCountReport logCounts();

    @JsonProperty("rpi")
    @Nullable
    public abstract String rpi();

    @JsonProperty("rpi_temperature")
    @Nullable
    public abstract Double rpiTemperature();

    @JsonProperty("configuration")
    @Nullable
    public abstract String configuration();

    @JsonProperty("last_report")
    @Nullable
    public abstract DateTime lastReport();

    public static ConnectTapStatusReport create(String version, String uuid, String name, Long throughput, DateTime localTime, String remoteAddress, Double cpuUtilization, Double memoryUtilization, String organizationName, String tenantName, ConnectTapLogCountReport logCounts, String rpi, Double rpiTemperature, String configuration, DateTime lastReport) {
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
                .rpi(rpi)
                .rpiTemperature(rpiTemperature)
                .configuration(configuration)
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

        public abstract Builder throughput(Long throughput);

        public abstract Builder localTime(DateTime localTime);

        public abstract Builder remoteAddress(String remoteAddress);

        public abstract Builder cpuUtilization(Double cpuUtilization);

        public abstract Builder memoryUtilization(Double memoryUtilization);

        public abstract Builder organizationName(String organizationName);

        public abstract Builder tenantName(String tenantName);

        public abstract Builder logCounts(ConnectTapLogCountReport logCounts);

        public abstract Builder rpi(String rpi);

        public abstract Builder rpiTemperature(Double rpiTemperature);

        public abstract Builder configuration(String configuration);

        public abstract Builder lastReport(DateTime lastReport);

        public abstract ConnectTapStatusReport build();
    }
}
