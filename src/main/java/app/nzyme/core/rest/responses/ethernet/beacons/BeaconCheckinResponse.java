package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressResponse;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class BeaconCheckinResponse {

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    @JsonProperty("source_uuid")
    public abstract UUID sourceUuid();

    @JsonProperty("source_address")
    public abstract String sourceAddress();

    @Nullable
    @JsonProperty("source_mac")
    public abstract String sourceMac();

    @Nullable
    @JsonProperty("destination_address")
    public abstract String destinationAddress();

    @Nullable
    @JsonProperty("destination_mac")
    public abstract String destinationMac();

    @Nullable
    @JsonProperty("destination_mac_data")
    public abstract EthernetMacAddressResponse destinationMacData();

    @Nullable
    @JsonProperty("source")
    public abstract L4AddressResponse source();

    @Nullable
    @JsonProperty("destination")
    public abstract L4AddressResponse destination();

    @Nullable
    @JsonProperty("tap_uuid")
    public abstract UUID tapUuid();

    @Nullable
    @JsonProperty("tap_name")
    public abstract String tapName();

    @Nullable
    @JsonProperty("session_key")
    public abstract String sessionKey();

    @Nullable
    @JsonProperty("l4_type")
    public abstract String l4Type();

    @JsonProperty("session_count")
    public abstract int sessionCount();

    @Nullable
    @JsonProperty("bytes_rx")
    public abstract Long bytesRx();

    @Nullable
    @JsonProperty("bytes_tx")
    public abstract Long bytesTx();

    @Nullable
    @JsonProperty("bytes_total")
    public abstract Long bytesTotal();

    public static BeaconCheckinResponse create(DateTime timestamp, UUID sourceUuid, String sourceAddress, String sourceMac, String destinationAddress, String destinationMac, EthernetMacAddressResponse destinationMacData, L4AddressResponse source, L4AddressResponse destination, UUID tapUuid, String tapName, String sessionKey, String l4Type, int sessionCount, Long bytesRx, Long bytesTx, Long bytesTotal) {
        return builder()
                .timestamp(timestamp)
                .sourceUuid(sourceUuid)
                .sourceAddress(sourceAddress)
                .sourceMac(sourceMac)
                .destinationAddress(destinationAddress)
                .destinationMac(destinationMac)
                .destinationMacData(destinationMacData)
                .source(source)
                .destination(destination)
                .tapUuid(tapUuid)
                .tapName(tapName)
                .sessionKey(sessionKey)
                .l4Type(l4Type)
                .sessionCount(sessionCount)
                .bytesRx(bytesRx)
                .bytesTx(bytesTx)
                .bytesTotal(bytesTotal)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconCheckinResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);
        public abstract Builder sourceUuid(UUID sourceUuid);
        public abstract Builder sourceAddress(String sourceAddress);
        public abstract Builder sourceMac(String sourceMac);
        public abstract Builder destinationAddress(String destinationAddress);
        public abstract Builder destinationMac(String destinationMac);

        public abstract Builder destinationMacData(EthernetMacAddressResponse destinationMacData);
        public abstract Builder source(L4AddressResponse source);
        public abstract Builder destination(L4AddressResponse destination);
        public abstract Builder tapUuid(UUID tapUuid);
        public abstract Builder tapName(String tapName);
        public abstract Builder sessionKey(String sessionKey);
        public abstract Builder l4Type(String l4Type);
        public abstract Builder sessionCount(int sessionCount);
        public abstract Builder bytesRx(Long bytesRx);
        public abstract Builder bytesTx(Long bytesTx);
        public abstract Builder bytesTotal(Long bytesTotal);
        public abstract BeaconCheckinResponse build();
    }

}
