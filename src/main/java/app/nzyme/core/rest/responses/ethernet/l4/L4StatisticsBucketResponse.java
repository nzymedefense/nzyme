package app.nzyme.core.rest.responses.ethernet.l4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class L4StatisticsBucketResponse {

    @JsonProperty("bytes_tcp")
    public abstract long bytesTcp();

    @JsonProperty("bytes_internal_tcp")
    public abstract long bytesInternalTcp();

    @JsonProperty("bytes_udp")
    public abstract long bytesUdp();

    @JsonProperty("bytes_internal_udp")
    public abstract long bytesInternalUdp();

    @JsonProperty("segments_tcp")
    public abstract long segmentsTcp();

    @JsonProperty("datagrams_udp")
    public abstract long datagramsUdp();

    @JsonProperty("sessions_tcp")
    public abstract long sessionsTcp();

    @JsonProperty("sessions_udp")
    public abstract long sessionsUdp();

    @JsonProperty("sessions_internal_tcp")
    public abstract long sessionsInternalTcp();

    @JsonProperty("sessions_internal_udp")
    public abstract long sessionsInternalUdp();

    public static L4StatisticsBucketResponse create(long bytesTcp, long bytesInternalTcp, long bytesUdp, long bytesInternalUdp, long segmentsTcp, long datagramsUdp, long sessionsTcp, long sessionsUdp, long sessionsInternalTcp, long sessionsInternalUdp) {
        return builder()
                .bytesTcp(bytesTcp)
                .bytesInternalTcp(bytesInternalTcp)
                .bytesUdp(bytesUdp)
                .bytesInternalUdp(bytesInternalUdp)
                .segmentsTcp(segmentsTcp)
                .datagramsUdp(datagramsUdp)
                .sessionsTcp(sessionsTcp)
                .sessionsUdp(sessionsUdp)
                .sessionsInternalTcp(sessionsInternalTcp)
                .sessionsInternalUdp(sessionsInternalUdp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_L4StatisticsBucketResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bytesTcp(long bytesTcp);

        public abstract Builder bytesInternalTcp(long bytesInternalTcp);

        public abstract Builder bytesUdp(long bytesUdp);

        public abstract Builder bytesInternalUdp(long bytesInternalUdp);

        public abstract Builder segmentsTcp(long segmentsTcp);

        public abstract Builder datagramsUdp(long datagramsUdp);

        public abstract Builder sessionsTcp(long sessionsTcp);

        public abstract Builder sessionsUdp(long sessionsUdp);

        public abstract Builder sessionsInternalTcp(long sessionsInternalTcp);

        public abstract Builder sessionsInternalUdp(long sessionsInternalUdp);

        public abstract L4StatisticsBucketResponse build();
    }
}
