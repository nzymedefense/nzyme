package app.nzyme.core.rest.resources.taps.reports.tables.arp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ArpPacketReport {

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

    @JsonCreator
    public static ArpPacketReport create(@JsonProperty("ethernet_source_mac") String ethernetSourceMac,
                                         @JsonProperty("ethernet_destination_mac") String ethernetDestinationMac,
                                         @JsonProperty("hardware_type") String hardwareType,
                                         @JsonProperty("protocol_type") String protocolType,
                                         @JsonProperty("operation") String operation,
                                         @JsonProperty("arp_sender_mac") String arpSenderMac,
                                         @JsonProperty("arp_sender_address") String arpSenderAddress,
                                         @JsonProperty("arp_target_mac") String arpTargetMac,
                                         @JsonProperty("arp_target_address") String arpTargetAddress,
                                         @JsonProperty("size") int size,
                                         @JsonProperty("timestamp") DateTime timestamp) {
        return builder()
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
        return new AutoValue_ArpPacketReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
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

        public abstract ArpPacketReport build();
    }
}
