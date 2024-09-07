package app.nzyme.core.rest.resources.taps.reports.context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TapMacContextReport {

    public abstract String mac();
    public abstract List<TapContextDataReport> ipAddresses();
    public abstract List<TapContextDataReport> hostnames();

    @JsonCreator
    public static TapMacContextReport create(@JsonProperty("mac") String mac,
                                             @JsonProperty("ip_addresses") List<TapContextDataReport> ipAddresses,
                                             @JsonProperty("hostnames") List<TapContextDataReport> hostnames) {
        return builder()
                .mac(mac)
                .ipAddresses(ipAddresses)
                .hostnames(hostnames)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapMacContextReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(String mac);

        public abstract Builder ipAddresses(List<TapContextDataReport> ipAddresses);

        public abstract Builder hostnames(List<TapContextDataReport> hostnames);

        public abstract TapMacContextReport build();
    }
}
