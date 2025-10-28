package app.nzyme.core.rest.responses.ethernet.l4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class L4StatisticsBucketResponse {

    @JsonProperty("bytes_rx_tcp")
    public abstract long bytesRxTcp();

    @JsonProperty("bytes_tx_tcp")
    public abstract long bytesTxTcp();

    @JsonProperty("bytes_rx_internal_tcp")
    public abstract long bytesRxInternalTcp();

    @JsonProperty("bytes_tx_internal_tcp")
    public abstract long bytesTxInternalTcp();

    @JsonProperty("bytes_rx_udp")
    public abstract long bytesRxUdp();

    @JsonProperty("bytes_tx_udp")
    public abstract long bytesTxUdp();

    @JsonProperty("bytes_rx_internal_udp")
    public abstract long bytesRxInternalUdp();

    @JsonProperty("bytes_tx_internal_udp")
    public abstract long bytesTxInternalUdp();

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

    public static L4StatisticsBucketResponse create(long bytesRxTcp, long bytesTxTcp, long bytesRxInternalTcp, long bytesTxInternalTcp, long bytesRxUdp, long bytesTxUdp, long bytesRxInternalUdp, long bytesTxInternalUdp, long segmentsTcp, long datagramsUdp, long sessionsTcp, long sessionsUdp, long sessionsInternalTcp, long sessionsInternalUdp) {
        return builder()
                .bytesRxTcp(bytesRxTcp)
                .bytesTxTcp(bytesTxTcp)
                .bytesRxInternalTcp(bytesRxInternalTcp)
                .bytesTxInternalTcp(bytesTxInternalTcp)
                .bytesRxUdp(bytesRxUdp)
                .bytesTxUdp(bytesTxUdp)
                .bytesRxInternalUdp(bytesRxInternalUdp)
                .bytesTxInternalUdp(bytesTxInternalUdp)
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
        public abstract Builder bytesRxTcp(long bytesRxTcp);

        public abstract Builder bytesTxTcp(long bytesTxTcp);

        public abstract Builder bytesRxInternalTcp(long bytesRxInternalTcp);

        public abstract Builder bytesTxInternalTcp(long bytesTxInternalTcp);

        public abstract Builder bytesRxUdp(long bytesRxUdp);

        public abstract Builder bytesTxUdp(long bytesTxUdp);

        public abstract Builder bytesRxInternalUdp(long bytesRxInternalUdp);

        public abstract Builder bytesTxInternalUdp(long bytesTxInternalUdp);

        public abstract Builder segmentsTcp(long segmentsTcp);

        public abstract Builder datagramsUdp(long datagramsUdp);

        public abstract Builder sessionsTcp(long sessionsTcp);

        public abstract Builder sessionsUdp(long sessionsUdp);

        public abstract Builder sessionsInternalTcp(long sessionsInternalTcp);

        public abstract Builder sessionsInternalUdp(long sessionsInternalUdp);

        public abstract L4StatisticsBucketResponse build();
    }
}
