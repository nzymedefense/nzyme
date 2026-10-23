package app.nzyme.core.rest.responses.ethernet.recon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ReconScanAnsweredHostResponse {

    @JsonProperty("address")
    public abstract String address();

    @JsonProperty("ports")
    public abstract List<Integer> ports();

    public static ReconScanAnsweredHostResponse create(String address, List<Integer> ports) {
        return builder()
                .address(address)
                .ports(ports)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconScanAnsweredHostResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder address(String address);
        public abstract Builder ports(List<Integer> ports);
        public abstract ReconScanAnsweredHostResponse build();
    }
}
