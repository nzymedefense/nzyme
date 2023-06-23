package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class SSIDSummaryDetailsResponse {

    @JsonProperty("ssid")
    public abstract String ssid();

    @JsonProperty("signal_strength_average")
    public abstract float signalStrengthAverage();

    @JsonProperty("security_protocols")
    public abstract List<String> securityProtocols();

    @JsonProperty("is_wps")
    public abstract List<Boolean> isWps();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static SSIDSummaryDetailsResponse create(String ssid, float signalStrengthAverage, List<String> securityProtocols, List<Boolean> isWps, DateTime lastSeen) {
        return builder()
                .ssid(ssid)
                .signalStrengthAverage(signalStrengthAverage)
                .securityProtocols(securityProtocols)
                .isWps(isWps)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDSummaryDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract Builder signalStrengthAverage(float signalStrengthAverage);

        public abstract Builder securityProtocols(List<String> securityProtocols);

        public abstract Builder isWps(List<Boolean> isWps);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract SSIDSummaryDetailsResponse build();
    }
}
