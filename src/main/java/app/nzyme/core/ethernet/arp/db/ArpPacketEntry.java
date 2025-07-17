package app.nzyme.core.ethernet.arp.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class ArpPacketEntry {

    public abstract UUID tapUUID();
    public abstract String ethernetSourceMac();
    public abstract String ethernetDestinationMac();
    public abstract String hardwareType();
    public abstract String protocolType();
    public abstract String operation();
    public abstract String arpSenderMac();
    public abstract String arpSenderAddress();
    public abstract String arpTargetMac();
    public abstract String arpTargetAddress();
    public abstract int size();
    public abstract DateTime timestamp();

    public static ArpPacketEntry create(UUID tapUUID, String ethernetSourceMac, String ethernetDestinationMac, String hardwareType, String protocolType, String operation, String arpSenderMac, String arpSenderAddress, String arpTargetMac, String arpTargetAddress, int size, DateTime timestamp) {
        return builder()
                .tapUUID(tapUUID)
                .ethernetSourceMac(ethernetSourceMac)
                .ethernetDestinationMac(ethernetDestinationMac)
                .hardwareType(hardwareType)
                .protocolType(protocolType)
                .operation(operation)
                .arpSenderMac(arpSenderMac)
                .arpSenderAddress(arpSenderAddress)
                .arpTargetMac(arpTargetMac)
                .arpTargetAddress(arpTargetAddress)
                .size(size)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ArpPacketEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tapUUID(UUID tapUUID);

        public abstract Builder ethernetSourceMac(String ethernetSourceMac);

        public abstract Builder ethernetDestinationMac(String ethernetDestinationMac);

        public abstract Builder hardwareType(String hardwareType);

        public abstract Builder protocolType(String protocolType);

        public abstract Builder operation(String operation);

        public abstract Builder arpSenderMac(String arpSenderMac);

        public abstract Builder arpSenderAddress(String arpSenderAddress);

        public abstract Builder arpTargetMac(String arpTargetMac);

        public abstract Builder arpTargetAddress(String arpTargetAddress);

        public abstract Builder size(int size);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract ArpPacketEntry build();
    }
}
