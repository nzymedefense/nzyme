package app.nzyme.core.rest.responses.ethernet.recon;

import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class ReconScanTargetResponse {

    @JsonProperty("target_address")
    public abstract String targetAddress();

    @Nullable
    @JsonProperty("target_address_data")
    public abstract L4AddressResponse targetAddressData();

    @Nullable
    @JsonProperty("target_port")
    public abstract Integer targetPort();

    @JsonProperty("outcome")
    public abstract String outcome();

    @JsonProperty("probe_count")
    public abstract long probeCount();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static ReconScanTargetResponse create(String targetAddress, L4AddressResponse targetAddressData, Integer targetPort, String outcome, long probeCount, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .targetAddress(targetAddress)
                .targetAddressData(targetAddressData)
                .targetPort(targetPort)
                .outcome(outcome)
                .probeCount(probeCount)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconScanTargetResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder targetAddress(String targetAddress);
        public abstract Builder targetAddressData(L4AddressResponse targetAddressData);
        public abstract Builder targetPort(Integer targetPort);
        public abstract Builder outcome(String outcome);
        public abstract Builder probeCount(long probeCount);
        public abstract Builder firstSeen(DateTime firstSeen);
        public abstract Builder lastSeen(DateTime lastSeen);
        public abstract ReconScanTargetResponse build();
    }
}
