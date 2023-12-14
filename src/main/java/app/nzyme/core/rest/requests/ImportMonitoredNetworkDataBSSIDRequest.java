package app.nzyme.core.rest.requests;

import app.nzyme.core.rest.constraints.MacAddress;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@AutoValue
public abstract class ImportMonitoredNetworkDataBSSIDRequest {

    @MacAddress
    public abstract String bssid();

    @NotNull
    public abstract List<String> fingerprints();

    @JsonCreator
    public static ImportMonitoredNetworkDataBSSIDRequest create(@JsonProperty("bssid") String bssid,
                                                                @JsonProperty("fingerprints") List<String> fingerprints) {
        return builder()
                .bssid(bssid)
                .fingerprints(fingerprints)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ImportMonitoredNetworkDataBSSIDRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(String bssid);

        public abstract Builder fingerprints(@NotNull List<String> fingerprints);

        public abstract ImportMonitoredNetworkDataBSSIDRequest build();
    }

}
