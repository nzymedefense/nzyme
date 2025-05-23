package app.nzyme.core.rest.responses.ethernet.dhcp;

import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class DHCPTransactionDetailsResponse {

    @JsonProperty("transaction_id")
    public abstract long transactionId();

    @JsonProperty("transaction_type")
    public abstract String transactionType();

    @JsonProperty("client_mac")
    public abstract EthernetMacAddressResponse clientMac();

    @JsonProperty("additional_client_macs")
    public abstract List<String> additionalClientMacs();

    @JsonProperty("server_mac")
    @Nullable
    public abstract EthernetMacAddressResponse serverMac();

    @JsonProperty("additional_server_macs")
    public abstract List<String> additionalServerMacs();

    @JsonProperty("offered_ip_addresses")
    public abstract List<String> offeredIpAddresses();

    @JsonProperty("requested_ip_address")
    @Nullable
    public abstract String requestedIpAddress();

    @JsonProperty("options_fingerprint")
    @Nullable
    public abstract String optionsFingerprint();

    @JsonProperty("additional_options_fingerprints")
    public abstract List<String> additionalOptionsFingerprints();

    @JsonProperty("timestamps")
    public abstract Map<String, List<DateTime>> timestamps();

    @JsonProperty("first_packet")
    @Nullable
    public abstract DateTime firstPacket();

    @JsonProperty("latest_packet")
    public abstract DateTime latestPacket();

    @JsonProperty("notes")
    public abstract List<String> notes();

    @JsonProperty("is_complete")
    public abstract boolean isComplete();

    @JsonProperty("duration_ms")
    public abstract Long durationMs();

    public static DHCPTransactionDetailsResponse create(long transactionId, String transactionType, EthernetMacAddressResponse clientMac, List<String> additionalClientMacs, EthernetMacAddressResponse serverMac, List<String> additionalServerMacs, List<String> offeredIpAddresses, String requestedIpAddress, String optionsFingerprint, List<String> additionalOptionsFingerprints, Map<String, List<DateTime>> timestamps, DateTime firstPacket, DateTime latestPacket, List<String> notes, boolean isComplete, Long durationMs) {
        return builder()
                .transactionId(transactionId)
                .transactionType(transactionType)
                .clientMac(clientMac)
                .additionalClientMacs(additionalClientMacs)
                .serverMac(serverMac)
                .additionalServerMacs(additionalServerMacs)
                .offeredIpAddresses(offeredIpAddresses)
                .requestedIpAddress(requestedIpAddress)
                .optionsFingerprint(optionsFingerprint)
                .additionalOptionsFingerprints(additionalOptionsFingerprints)
                .timestamps(timestamps)
                .firstPacket(firstPacket)
                .latestPacket(latestPacket)
                .notes(notes)
                .isComplete(isComplete)
                .durationMs(durationMs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DHCPTransactionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder transactionId(long transactionId);

        public abstract Builder transactionType(String transactionType);

        public abstract Builder clientMac(EthernetMacAddressResponse clientMac);

        public abstract Builder additionalClientMacs(List<String> additionalClientMacs);

        public abstract Builder serverMac(EthernetMacAddressResponse serverMac);

        public abstract Builder additionalServerMacs(List<String> additionalServerMacs);

        public abstract Builder offeredIpAddresses(List<String> offeredIpAddresses);

        public abstract Builder requestedIpAddress(String requestedIpAddress);

        public abstract Builder optionsFingerprint(String optionsFingerprint);

        public abstract Builder additionalOptionsFingerprints(List<String> additionalOptionsFingerprints);

        public abstract Builder timestamps(Map<String, List<DateTime>> timestamps);

        public abstract Builder firstPacket(DateTime firstPacket);

        public abstract Builder latestPacket(DateTime latestPacket);

        public abstract Builder notes(List<String> notes);

        public abstract Builder isComplete(boolean isComplete);

        public abstract Builder durationMs(Long durationMs);

        public abstract DHCPTransactionDetailsResponse build();
    }
}
