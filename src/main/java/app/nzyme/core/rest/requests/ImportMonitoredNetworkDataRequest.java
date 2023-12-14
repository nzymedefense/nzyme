package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@AutoValue
public abstract class ImportMonitoredNetworkDataRequest {

    @NotNull
    public abstract List<ImportMonitoredNetworkDataBSSIDRequest> bssids();

    @NotNull
    public abstract List<Long> channels();

    @NotNull
    public abstract List<String> securitySuites();

    @JsonCreator
    public static ImportMonitoredNetworkDataRequest create(@JsonProperty("bssids") List<ImportMonitoredNetworkDataBSSIDRequest> bssids,
                                                           @JsonProperty("channels") List<Long> channels,
                                                           @JsonProperty("security_suites") List<String> securitySuites) {
        return builder()
                .bssids(bssids)
                .channels(channels)
                .securitySuites(securitySuites)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ImportMonitoredNetworkDataRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssids(@NotNull List<ImportMonitoredNetworkDataBSSIDRequest> bssids);

        public abstract Builder channels(@NotNull List<Long> channels);

        public abstract Builder securitySuites(@NotNull List<String> securitySuites);

        public abstract ImportMonitoredNetworkDataRequest build();
    }

}
