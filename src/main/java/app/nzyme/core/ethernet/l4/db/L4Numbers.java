package app.nzyme.core.ethernet.l4.db;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class L4Numbers {

    public abstract long bytesTcp();
    public abstract long bytesInternalTcp();
    public abstract long bytesUdp();
    public abstract long bytesInternalUdp();
    public abstract long segmentsTcp();
    public abstract long datagramsUdp();

    public static L4Numbers create(long bytesTcp, long bytesInternalTcp, long bytesUdp, long bytesInternalUdp, long segmentsTcp, long datagramsUdp) {
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
        return new AutoValue_L4Numbers.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bytesTcp(long bytesTcp);

        public abstract Builder bytesInternalTcp(long bytesInternalTcp);

        public abstract Builder bytesUdp(long bytesUdp);

        public abstract Builder bytesInternalUdp(long bytesInternalUdp);

        public abstract Builder segmentsTcp(long segmentsTcp);

        public abstract Builder datagramsUdp(long datagramsUdp);

        public abstract L4Numbers build();
    }

}
