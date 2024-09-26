package app.nzyme.core.rest.responses.dot11.clients;

import app.nzyme.core.rest.responses.context.MacAddressTransparentHostnameResponse;
import app.nzyme.core.rest.responses.context.MacAddressTransparentIpAddressResponse;
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

    @JsonProperty("transparent_ip_addresses")
    public abstract List<MacAddressTransparentIpAddressResponse> transparentIpAddresses();

    @JsonProperty("transparent_hostnames")
    public abstract List<MacAddressTransparentHostnameResponse> transparentHostnames();

    @JsonProperty("probe_requests")
    public abstract List<String> probeRequests();

    @JsonProperty("connected_signal_strength")
    public abstract List<TapBasedSignalStrengthResponse> connectedSignalStrength();

    @JsonProperty("disconnected_signal_strength")
    public abstract List<TapBasedSignalStrengthResponse> disconnectedSignalStrength();

    @JsonProperty("data_retention_days")
    public abstract int dataRetentionDays();

    public static ClientDetailsResponse create(Dot11MacAddressResponse mac, ConnectedBSSID connectedBSSID, List<ConnectedBSSID> connectedBSSIDHistory, DateTime firstSeen, DateTime lastSeen, List<MacAddressTransparentIpAddressResponse> transparentIpAddresses, List<MacAddressTransparentHostnameResponse> transparentHostnames, List<String> probeRequests, List<TapBasedSignalStrengthResponse> connectedSignalStrength, List<TapBasedSignalStrengthResponse> disconnectedSignalStrength, int dataRetentionDays) {
        return builder()
                .mac(mac)
                .connectedBSSID(connectedBSSID)
                .connectedBSSIDHistory(connectedBSSIDHistory)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .transparentIpAddresses(transparentIpAddresses)
                .transparentHostnames(transparentHostnames)
                .probeRequests(probeRequests)
                .connectedSignalStrength(connectedSignalStrength)
                .disconnectedSignalStrength(disconnectedSignalStrength)
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

        public abstract Builder transparentIpAddresses(List<MacAddressTransparentIpAddressResponse> transparentIpAddresses);

        public abstract Builder transparentHostnames(List<MacAddressTransparentHostnameResponse> transparentHostnames);

        public abstract Builder probeRequests(List<String> probeRequests);

        public abstract Builder connectedSignalStrength(List<TapBasedSignalStrengthResponse> connectedSignalStrength);

        public abstract Builder disconnectedSignalStrength(List<TapBasedSignalStrengthResponse> disconnectedSignalStrength);

        public abstract Builder dataRetentionDays(int dataRetentionDays);

        public abstract ClientDetailsResponse build();
    }
}
