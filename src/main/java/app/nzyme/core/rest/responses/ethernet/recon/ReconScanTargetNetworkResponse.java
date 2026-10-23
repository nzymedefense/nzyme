package app.nzyme.core.rest.responses.ethernet.recon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ReconScanTargetNetworkResponse {

    @JsonProperty("network")
    public abstract String network();

    @JsonProperty("hosts")
    public abstract long hosts();

    @JsonProperty("answered_hosts")
    public abstract long answeredHosts();

    @JsonProperty("ports")
    public abstract long ports();

    public static ReconScanTargetNetworkResponse create(String network, long hosts, long answeredHosts, long ports) {
        return builder()
                .network(network)
                .hosts(hosts)
                .answeredHosts(answeredHosts)
                .ports(ports)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconScanTargetNetworkResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder network(String network);
        public abstract Builder hosts(long hosts);
        public abstract Builder answeredHosts(long answeredHosts);
        public abstract Builder ports(long ports);
        public abstract ReconScanTargetNetworkResponse build();
    }
}
