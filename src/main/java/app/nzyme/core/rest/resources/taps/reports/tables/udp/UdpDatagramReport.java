package app.nzyme.core.rest.resources.taps.reports.tables.udp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class UdpDatagramReport {

    @Nullable
    public abstract String sourceMac();
    @Nullable
    public abstract String destinationMac();
    public abstract String sourceAddress();
    public abstract int sourcePort();
    public abstract String destinationAddress();
    public abstract int destinationPort();
    public abstract long bytesCount();
    public abstract DateTime timestamp();
    @Nullable
    public abstract List<String> tags();

    @JsonCreator
    public static UdpDatagramReport create(@JsonProperty("source_mac") String sourceMac,
                                           @JsonProperty("destination_mac") String destinationMac,
                                           @JsonProperty("source_address") String sourceAddress,
                                           @JsonProperty("source_port") int sourcePort,
                                           @JsonProperty("destination_address") String destinationAddress,
                                           @JsonProperty("destination_port") int destinationPort,
                                           @JsonProperty("bytes_count") long bytesCount,
                                           @JsonProperty("timestamp") DateTime timestamp,
                                           @JsonProperty("tags") List<String> tags) {
        return builder()
                .sourceMac(sourceMac)
                .destinationMac(destinationMac)
                .sourceAddress(sourceAddress)
                .sourcePort(sourcePort)
                .destinationAddress(destinationAddress)
                .destinationPort(destinationPort)
                .bytesCount(bytesCount)
                .timestamp(timestamp)
                .tags(tags)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UdpDatagramReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sourceMac(String sourceMac);

        public abstract Builder destinationMac(String destinationMac);

        public abstract Builder sourceAddress(String sourceAddress);

        public abstract Builder sourcePort(int sourcePort);

        public abstract Builder destinationAddress(String destinationAddress);

        public abstract Builder destinationPort(int destinationPort);

        public abstract Builder bytesCount(long bytesCount);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder tags(List<String> tags);

        public abstract UdpDatagramReport build();
    }

}
