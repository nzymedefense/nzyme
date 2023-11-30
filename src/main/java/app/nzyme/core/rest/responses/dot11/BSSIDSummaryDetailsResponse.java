package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class BSSIDSummaryDetailsResponse {

    @JsonProperty("bssid")
    public abstract Dot11MacAddressResponse bssid();

    @JsonProperty("oui")
    @Nullable
    public abstract String oui();

    @JsonProperty("security_protocols")
    @Nullable
    public abstract List<String> securityProtocols();

    @JsonProperty("signal_strength_average")
    public abstract float signalStrengthAverage();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("client_count")
    public abstract long clientCount();

    @JsonProperty("fingerprints")
    public abstract List<String> fingerprints();

    @JsonProperty("advertised_ssid_names")
    public abstract List<String> advertisedSSIDNames();

    @JsonProperty("has_hidden_ssid_advertisements")
    public abstract boolean hasHiddenSSIDAdvertisements();

    @JsonProperty("infrastructure_types")
    public abstract List<String> infrastructureTypes();

    public static BSSIDSummaryDetailsResponse create(Dot11MacAddressResponse bssid, String oui, List<String> securityProtocols, float signalStrengthAverage, DateTime firstSeen, DateTime lastSeen, long clientCount, List<String> fingerprints, List<String> advertisedSSIDNames, boolean hasHiddenSSIDAdvertisements, List<String> infrastructureTypes) {
        return builder()
                .bssid(bssid)
                .oui(oui)
                .securityProtocols(securityProtocols)
                .signalStrengthAverage(signalStrengthAverage)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .clientCount(clientCount)
                .fingerprints(fingerprints)
                .advertisedSSIDNames(advertisedSSIDNames)
                .hasHiddenSSIDAdvertisements(hasHiddenSSIDAdvertisements)
                .infrastructureTypes(infrastructureTypes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDSummaryDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(Dot11MacAddressResponse bssid);

        public abstract Builder oui(String oui);

        public abstract Builder securityProtocols(List<String> securityProtocols);

        public abstract Builder signalStrengthAverage(float signalStrengthAverage);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder clientCount(long clientCount);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Builder advertisedSSIDNames(List<String> advertisedSSIDNames);

        public abstract Builder hasHiddenSSIDAdvertisements(boolean hasHiddenSSIDAdvertisements);

        public abstract Builder infrastructureTypes(List<String> infrastructureTypes);

        public abstract BSSIDSummaryDetailsResponse build();
    }
}
