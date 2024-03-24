package app.nzyme.core.rest.resources.taps.reports.tables.tcp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class TcpSessionReport {

    public abstract String state();
    public abstract String sourceAddress();
    public abstract int sourcePort();
    public abstract String destinationAddress();
    public abstract int destinationPort();
    public abstract DateTime startTime();
    @Nullable
    public abstract DateTime endTime();
    public abstract DateTime mostRecentSegmentTime();
    public abstract long segmentCount();
    public abstract long bytesCount();
    @Nullable
    public abstract List<String> tags();

    @JsonCreator
    public static TcpSessionReport create(@JsonProperty("state") String state,
                                          @JsonProperty("source_address") String sourceAddress,
                                          @JsonProperty("source_port") int sourcePort,
                                          @JsonProperty("destination_address") String destinationAddress,
                                          @JsonProperty("destination_port") int destinationPort,
                                          @JsonProperty("start_time") DateTime startTime,
                                          @JsonProperty("end_time") DateTime endTime,
                                          @JsonProperty("most_recent_segment_time") DateTime mostRecentSegmentTime,
                                          @JsonProperty("segment_count") long segmentCount,
                                          @JsonProperty("bytes_count") long bytesCount,
                                          @JsonProperty("tags") List<String> tags) {
        return builder()
                .state(state)
                .sourceAddress(sourceAddress)
                .sourcePort(sourcePort)
                .destinationAddress(destinationAddress)
                .destinationPort(destinationPort)
                .startTime(startTime)
                .endTime(endTime)
                .mostRecentSegmentTime(mostRecentSegmentTime)
                .segmentCount(segmentCount)
                .bytesCount(bytesCount)
                .tags(tags)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TcpSessionReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder state(String state);

        public abstract Builder sourceAddress(String sourceAddress);

        public abstract Builder sourcePort(int sourcePort);

        public abstract Builder destinationAddress(String destinationAddress);

        public abstract Builder destinationPort(int destinationPort);

        public abstract Builder startTime(DateTime startTime);

        public abstract Builder endTime(DateTime endTime);

        public abstract Builder mostRecentSegmentTime(DateTime mostRecentSegmentTime);

        public abstract Builder segmentCount(long segmentCount);

        public abstract Builder bytesCount(long bytesCount);

        public abstract Builder tags(List<String> tags);

        public abstract TcpSessionReport build();
    }

}
