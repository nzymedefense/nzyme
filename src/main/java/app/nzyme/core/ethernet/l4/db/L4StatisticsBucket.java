package app.nzyme.core.ethernet.l4.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class L4StatisticsBucket {

    public abstract DateTime bucket();
    public abstract long bytesRxTcp();
    public abstract long bytesTxTcp();
    public abstract long bytesRxInternalTcp();
    public abstract long bytesTxInternalTcp();
    public abstract long bytesRxUdp();
    public abstract long bytesTxUdp();
    public abstract long bytesRxInternalUdp();
    public abstract long bytesTxInternalUdp();
    public abstract long segmentsTcp();
    public abstract long datagramsUdp();
    public abstract long sessionsTcp();
    public abstract long sessionsUdp();
    public abstract long sessionsInternalTcp();
    public abstract long sessionsInternalUdp();

    public static L4StatisticsBucket create(DateTime bucket, long bytesRxTcp, long bytesTxTcp, long bytesRxInternalTcp, long bytesTxInternalTcp, long bytesRxUdp, long bytesTxUdp, long bytesRxInternalUdp, long bytesTxInternalUdp, long segmentsTcp, long datagramsUdp, long sessionsTcp, long sessionsUdp, long sessionsInternalTcp, long sessionsInternalUdp) {
        return builder()
                .bucket(bucket)
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
        return new AutoValue_L4StatisticsBucket.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

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

        public abstract L4StatisticsBucket build();
    }
}
