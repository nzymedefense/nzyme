package app.nzyme.core.ethernet.dhcp.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class DHCPTransactionEntry {

    public abstract long transactionId();
    public abstract String transactionType();
    public abstract String clientMac();
    public abstract List<String> additionalClientMacs();
    @Nullable
    public abstract String serverMac();
    public abstract List<String> additionalServerMacs();
    public abstract List<String> offeredIpAddresses();
    @Nullable
    public abstract String requestedIpAddress();
    @Nullable
    public abstract String optionsFingerprint();
    public abstract List<String> additionalOptionsFingerprints();
    public abstract Map<String, List<DateTime>> timestamps();
    @Nullable
    public abstract DateTime firstPacket();
    public abstract DateTime latestPacket();
    public abstract List<String> notes();
    public abstract boolean isSuccessful();
    public abstract boolean isComplete();

    public static DHCPTransactionEntry create(long transactionId, String transactionType, String clientMac, List<String> additionalClientMacs, String serverMac, List<String> additionalServerMacs, List<String> offeredIpAddresses, String requestedIpAddress, String optionsFingerprint, List<String> additionalOptionsFingerprints, Map<String, List<DateTime>> timestamps, DateTime firstPacket, DateTime latestPacket, List<String> notes, boolean isSuccessful, boolean isComplete) {
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
                .isSuccessful(isSuccessful)
                .isComplete(isComplete)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DHCPTransactionEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder transactionId(long transactionId);

        public abstract Builder transactionType(String transactionType);

        public abstract Builder clientMac(String clientMac);

        public abstract Builder additionalClientMacs(List<String> additionalClientMacs);

        public abstract Builder serverMac(String serverMac);

        public abstract Builder additionalServerMacs(List<String> additionalServerMacs);

        public abstract Builder offeredIpAddresses(List<String> offeredIpAddresses);

        public abstract Builder requestedIpAddress(String requestedIpAddress);

        public abstract Builder optionsFingerprint(String optionsFingerprint);

        public abstract Builder additionalOptionsFingerprints(List<String> additionalOptionsFingerprints);

        public abstract Builder timestamps(Map<String, List<DateTime>> timestamps);

        public abstract Builder firstPacket(DateTime firstPacket);

        public abstract Builder latestPacket(DateTime latestPacket);

        public abstract Builder notes(List<String> notes);

        public abstract Builder isSuccessful(boolean isSuccessful);

        public abstract Builder isComplete(boolean isComplete);

        public abstract DHCPTransactionEntry build();
    }
}
