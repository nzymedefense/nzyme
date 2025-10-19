package app.nzyme.core.ethernet.l4.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class L4StatisticsBucket {

    public abstract DateTime bucket();
    public abstract long bytesTcp();
    public abstract long bytesInternalTcp();
    public abstract long bytesUdp();
    public abstract long bytesInternalUdp();
    public abstract long segmentsTcp();
    public abstract long datagramsUdp();
    public abstract long sessionsTcp();
    public abstract long sessionsUdp();
    public abstract long sessionsInternalTcp();
    public abstract long sessionsInternalUdp();

    public static L4StatisticsBucket create(DateTime bucket, long bytesTcp, long bytesInternalTcp, long bytesUdp, long bytesInternalUdp, long segmentsTcp, long datagramsUdp, long sessionsTcp, long sessionsUdp, long sessionsInternalTcp, long sessionsInternalUdp) {
        return builder()
                .bucket(bucket)
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
        return new AutoValue_L4StatisticsBucket.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

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

        public abstract L4StatisticsBucket build();
    }
}
