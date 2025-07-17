package app.nzyme.core.rest.responses.ethernet.arp;

import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressResponse;
import app.nzyme.core.rest.responses.ethernet.InternalAddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class ArpPacketDetailsResponse {

    @JsonProperty("tap_uuid")
    public abstract UUID tapUUID();

    @JsonProperty("ethernet_source_mac")
    public abstract EthernetMacAddressResponse ethernetSourceMac();

    @JsonProperty("ethernet_destination_mac")
    public abstract EthernetMacAddressResponse ethernetDestinationMac();

    @JsonProperty("hardware_type")
    public abstract String hardwareType();

    @JsonProperty("protocol_type")
    public abstract String protocolType();

    @JsonProperty("operation")
    public abstract String operation();

    @JsonProperty("arp_sender")
    public abstract InternalAddressResponse arpSender();

    @JsonProperty("arp_target")
    public abstract InternalAddressResponse arpTarget();

    @JsonProperty("size")
    public abstract int size();

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    public static ArpPacketDetailsResponse create(UUID tapUUID, EthernetMacAddressResponse ethernetSourceMac, EthernetMacAddressResponse ethernetDestinationMac, String hardwareType, String protocolType, String operation, InternalAddressResponse arpSender, InternalAddressResponse arpTarget, int size, DateTime timestamp) {
        return builder()
                .tapUUID(tapUUID)
                .ethernetSourceMac(ethernetSourceMac)
                .ethernetDestinationMac(ethernetDestinationMac)
                .hardwareType(hardwareType)
                .protocolType(protocolType)
                .operation(operation)
                .arpSender(arpSender)
                .arpTarget(arpTarget)
                .size(size)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ArpPacketDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tapUUID(UUID tapUUID);

        public abstract Builder ethernetSourceMac(EthernetMacAddressResponse ethernetSourceMac);

        public abstract Builder ethernetDestinationMac(EthernetMacAddressResponse ethernetDestinationMac);

        public abstract Builder hardwareType(String hardwareType);

        public abstract Builder protocolType(String protocolType);

        public abstract Builder operation(String operation);

        public abstract Builder arpSender(InternalAddressResponse arpSender);

        public abstract Builder arpTarget(InternalAddressResponse arpTarget);

        public abstract Builder size(int size);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract ArpPacketDetailsResponse build();
    }
}
