package app.nzyme.core.rest.responses.dot11.monitoring.configimport;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class MonitoredNetworkImportDataResponse {

    @JsonProperty("bssids")
    public abstract List<BSSIDImportDataResponse> bssids();

    @JsonProperty("channels")
    public abstract List<ChannelImportDataResponse> channels();

    @JsonProperty("security_suites")
    public abstract List<SecuritySuiteImportDataResponse> securitySuites();

    public static MonitoredNetworkImportDataResponse create(List<BSSIDImportDataResponse> bssids, List<ChannelImportDataResponse> channels, List<SecuritySuiteImportDataResponse> securitySuites) {
        return builder()
                .bssids(bssids)
                .channels(channels)
                .securitySuites(securitySuites)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredNetworkImportDataResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssids(List<BSSIDImportDataResponse> bssids);

        public abstract Builder channels(List<ChannelImportDataResponse> channels);

        public abstract Builder securitySuites(List<SecuritySuiteImportDataResponse> securitySuites);

        public abstract MonitoredNetworkImportDataResponse build();
    }
}