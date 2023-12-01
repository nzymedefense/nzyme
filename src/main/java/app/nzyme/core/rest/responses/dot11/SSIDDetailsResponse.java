package app.nzyme.core.rest.responses.dot11;

import app.nzyme.core.dot11.db.BSSIDClientDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class SSIDDetailsResponse {

    @JsonProperty("bssid")
    public abstract Dot11MacAddressResponse bssid();

    @JsonProperty("ssid")
    public abstract String ssid();

    @JsonProperty("frequencies")
    public abstract List<Integer> frequencies();

    @JsonProperty("signal_strength_average")
    public abstract float signalStrengthAverage();

    @JsonProperty("total_frames")
    public abstract long totalFrames();

    @JsonProperty("total_bytes")
    public abstract long totalBytes();

    @JsonProperty("security_protocols")
    public abstract List<String> securityProtocols();

    @JsonProperty("fingerprints")
    public abstract List<String> fingerprints();

    @JsonProperty("access_point_clients")
    public abstract List<BSSIDClientDetails> accessPointClients();

    @JsonProperty("rates")
    public abstract List<Double> rates();

    @JsonProperty("infrastructure_types")
    public abstract List<String> infrastructureTypes();

    @JsonProperty("security_suites")
    public abstract List<SecuritySuitesResponse> securitySuites();

    @JsonProperty("is_wps")
    public abstract List<Boolean> isWps();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static SSIDDetailsResponse create(Dot11MacAddressResponse bssid, String ssid, List<Integer> frequencies, float signalStrengthAverage, long totalFrames, long totalBytes, List<String> securityProtocols, List<String> fingerprints, List<BSSIDClientDetails> accessPointClients, List<Double> rates, List<String> infrastructureTypes, List<SecuritySuitesResponse> securitySuites, List<Boolean> isWps, DateTime lastSeen) {
        return builder()
                .bssid(bssid)
                .ssid(ssid)
                .frequencies(frequencies)
                .signalStrengthAverage(signalStrengthAverage)
                .totalFrames(totalFrames)
                .totalBytes(totalBytes)
                .securityProtocols(securityProtocols)
                .fingerprints(fingerprints)
                .accessPointClients(accessPointClients)
                .rates(rates)
                .infrastructureTypes(infrastructureTypes)
                .securitySuites(securitySuites)
                .isWps(isWps)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(Dot11MacAddressResponse bssid);

        public abstract Builder ssid(String ssid);

        public abstract Builder frequencies(List<Integer> frequencies);

        public abstract Builder signalStrengthAverage(float signalStrengthAverage);

        public abstract Builder totalFrames(long totalFrames);

        public abstract Builder totalBytes(long totalBytes);

        public abstract Builder securityProtocols(List<String> securityProtocols);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Builder accessPointClients(List<BSSIDClientDetails> accessPointClients);

        public abstract Builder rates(List<Double> rates);

        public abstract Builder infrastructureTypes(List<String> infrastructureTypes);

        public abstract Builder securitySuites(List<SecuritySuitesResponse> securitySuites);

        public abstract Builder isWps(List<Boolean> isWps);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract SSIDDetailsResponse build();
    }
}
