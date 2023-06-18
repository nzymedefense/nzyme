package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class BSSIDDetailsResponse {

    @JsonProperty("bssid")
    public abstract String bssid();

    @JsonProperty("signal_strength_average")
    public abstract float signalStrengthAverage();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("fingerprints")
    public abstract List<String> fingerprints();

    @JsonProperty("advertised_ssid_names")
    public abstract List<String> advertisedSSIDNames();

    @JsonProperty("has_hidden_ssid_advertisements")
    public abstract boolean hasHiddenSSIDAdvertisements();

    public static BSSIDDetailsResponse create(String bssid, float signalStrengthAverage, DateTime lastSeen, List<String> fingerprints, List<String> advertisedSSIDNames, boolean hasHiddenSSIDAdvertisements) {
        return builder()
                .bssid(bssid)
                .signalStrengthAverage(signalStrengthAverage)
                .lastSeen(lastSeen)
                .fingerprints(fingerprints)
                .advertisedSSIDNames(advertisedSSIDNames)
                .hasHiddenSSIDAdvertisements(hasHiddenSSIDAdvertisements)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(String bssid);

        public abstract Builder signalStrengthAverage(float signalStrengthAverage);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Builder advertisedSSIDNames(List<String> advertisedSSIDNames);

        public abstract Builder hasHiddenSSIDAdvertisements(boolean hasHiddenSSIDAdvertisements);

        public abstract BSSIDDetailsResponse build();
    }

}
