package app.nzyme.core.rest.resources.taps.reports.tables.retro.l4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class L4RetroPairReport {

    public abstract String l4Type();
    public abstract String sourceMac();
    public abstract String destinationMac();
    public abstract String sourceAddress();
    public abstract String destinationAddress();
    public abstract Integer sourcePort();
    public abstract Integer destinationPort();
    public abstract Long connectionCount();
    public abstract Long size();

    public abstract DateTime timestamp();

    @JsonCreator
    public static L4RetroPairReport create(@JsonProperty("l4_type") String l4Type,
                                           @JsonProperty("source_mac") String sourceMac,
                                           @JsonProperty("destination_mac") String destinationMac,
                                           @JsonProperty("source_address") String sourceAddress,
                                           @JsonProperty("destination_address") String destinationAddress,
                                           @JsonProperty("source_port") Integer sourcePort,
                                           @JsonProperty("destination_port") Integer destinationPort,
                                           @JsonProperty("connection_count") Long connectionCount,
                                           @JsonProperty("size") Long size,
                                           @JsonProperty("timestamp") DateTime timestamp) {
        return builder()
                .l4Type(l4Type)
                .sourceMac(sourceMac)
                .destinationMac(destinationMac)
                .sourceAddress(sourceAddress)
                .destinationAddress(destinationAddress)
                .sourcePort(sourcePort)
                .destinationPort(destinationPort)
                .connectionCount(connectionCount)
                .size(size)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_L4RetroPairReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder l4Type(String l4Type);

        public abstract Builder sourceMac(String sourceMac);

        public abstract Builder destinationMac(String destinationMac);

        public abstract Builder sourceAddress(String sourceAddress);

        public abstract Builder destinationAddress(String destinationAddress);

        public abstract Builder sourcePort(Integer sourcePort);

        public abstract Builder destinationPort(Integer destinationPort);

        public abstract Builder connectionCount(Long connectionCount);

        public abstract Builder size(Long size);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract L4RetroPairReport build();
    }

}
