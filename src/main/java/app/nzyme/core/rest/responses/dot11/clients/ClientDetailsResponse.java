package app.nzyme.core.rest.responses.dot11.clients;

import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
import app.nzyme.core.rest.responses.shared.TapBasedSignalStrengthResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class ClientDetailsResponse {

    @JsonProperty("mac")
    public abstract Dot11MacAddressResponse mac();

    @JsonProperty("connected_bssid")
    @Nullable
    public abstract ConnectedBSSID connectedBSSID();

    @JsonProperty("connected_bssid_history")
    public abstract List<ConnectedBSSID> connectedBSSIDHistory();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("probe_requests")
    public abstract List<String> probeRequests();

    @JsonProperty("activity_histogram")
    public abstract Map<DateTime, ClientActivityHistogramValueResponse> activityHistogram();

    @JsonProperty("connected_signal_strength")
    public abstract List<TapBasedSignalStrengthResponse> connectedSignalStrength();

    @JsonProperty("disconnected_signal_strength")
    public abstract List<TapBasedSignalStrengthResponse> disconnectedSignalStrength();

    @JsonProperty("connected_signal_strength_histogram")
    public abstract List<ClientSignalStrengthResponse> connectedSignalStrengthHistogram();

    @JsonProperty("disconnected_signal_strength_histogram")
    public abstract List<ClientSignalStrengthResponse> disconnectedSignalStrengthHistogram();

    @JsonProperty("data_retention_days")
    public abstract int dataRetentionDays();

    public static ClientDetailsResponse create(Dot11MacAddressResponse mac, ConnectedBSSID connectedBSSID, List<ConnectedBSSID> connectedBSSIDHistory, DateTime firstSeen, DateTime lastSeen, List<String> probeRequests, Map<DateTime, ClientActivityHistogramValueResponse> activityHistogram, List<TapBasedSignalStrengthResponse> connectedSignalStrength, List<TapBasedSignalStrengthResponse> disconnectedSignalStrength, List<ClientSignalStrengthResponse> connectedSignalStrengthHistogram, List<ClientSignalStrengthResponse> disconnectedSignalStrengthHistogram, int dataRetentionDays) {
        return builder()
                .mac(mac)
                .connectedBSSID(connectedBSSID)
                .connectedBSSIDHistory(connectedBSSIDHistory)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .probeRequests(probeRequests)
                .activityHistogram(activityHistogram)
                .connectedSignalStrength(connectedSignalStrength)
                .disconnectedSignalStrength(disconnectedSignalStrength)
                .connectedSignalStrengthHistogram(connectedSignalStrengthHistogram)
                .disconnectedSignalStrengthHistogram(disconnectedSignalStrengthHistogram)
                .dataRetentionDays(dataRetentionDays)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(Dot11MacAddressResponse mac);

        public abstract Builder connectedBSSID(ConnectedBSSID connectedBSSID);

        public abstract Builder connectedBSSIDHistory(List<ConnectedBSSID> connectedBSSIDHistory);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder probeRequests(List<String> probeRequests);

        public abstract Builder activityHistogram(Map<DateTime, ClientActivityHistogramValueResponse> activityHistogram);

        public abstract Builder connectedSignalStrength(List<TapBasedSignalStrengthResponse> connectedSignalStrength);

        public abstract Builder disconnectedSignalStrength(List<TapBasedSignalStrengthResponse> disconnectedSignalStrength);

        public abstract Builder connectedSignalStrengthHistogram(List<ClientSignalStrengthResponse> connectedSignalStrengthHistogram);

        public abstract Builder disconnectedSignalStrengthHistogram(List<ClientSignalStrengthResponse> disconnectedSignalStrengthHistogram);

        public abstract Builder dataRetentionDays(int dataRetentionDays);

        public abstract ClientDetailsResponse build();
    }
}
