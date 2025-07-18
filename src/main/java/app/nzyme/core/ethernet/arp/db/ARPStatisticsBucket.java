package app.nzyme.core.ethernet.arp.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ARPStatisticsBucket {

    public abstract DateTime bucket();
    public abstract long totalCount();
    public abstract long requestCount();
    public abstract long replyCount();
    public abstract double requestToReplyRatio();
    public abstract long gratuitousRequestCount();
    public abstract long gratuitousReplyCount();

    public static ARPStatisticsBucket create(DateTime bucket, long totalCount, long requestCount, long replyCount, double requestToReplyRatio, long gratuitousRequestCount, long gratuitousReplyCount) {
        return builder()
                .bucket(bucket)
                .totalCount(totalCount)
                .requestCount(requestCount)
                .replyCount(replyCount)
                .requestToReplyRatio(requestToReplyRatio)
                .gratuitousRequestCount(gratuitousRequestCount)
                .gratuitousReplyCount(gratuitousReplyCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ARPStatisticsBucket.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder totalCount(long totalCount);

        public abstract Builder requestCount(long requestCount);

        public abstract Builder replyCount(long replyCount);

        public abstract Builder requestToReplyRatio(double requestToReplyRatio);

        public abstract Builder gratuitousRequestCount(long gratuitousRequestCount);

        public abstract Builder gratuitousReplyCount(long gratuitousReplyCount);

        public abstract ARPStatisticsBucket build();
    }
}
