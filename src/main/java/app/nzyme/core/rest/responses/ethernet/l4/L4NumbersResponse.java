package app.nzyme.core.rest.responses.ethernet.l4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class L4NumbersResponse {

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

    public static L4NumbersResponse create(long bytesTcp, long bytesInternalTcp, long bytesUdp, long bytesInternalUdp, long segmentsTcp, long datagramsUdp) {
        return builder()
                .bytesTcp(bytesTcp)
                .bytesInternalTcp(bytesInternalTcp)
                .bytesUdp(bytesUdp)
                .bytesInternalUdp(bytesInternalUdp)
                .segmentsTcp(segmentsTcp)
                .datagramsUdp(datagramsUdp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_L4NumbersResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bytesTcp(long bytesTcp);

        public abstract Builder bytesInternalTcp(long bytesInternalTcp);

        public abstract Builder bytesUdp(long bytesUdp);

        public abstract Builder bytesInternalUdp(long bytesInternalUdp);

        public abstract Builder segmentsTcp(long segmentsTcp);

        public abstract Builder datagramsUdp(long datagramsUdp);

        public abstract L4NumbersResponse build();
    }
}
