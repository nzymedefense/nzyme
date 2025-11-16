package app.nzyme.core.ethernet.dhcp.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DHCPStatisticsBucket {

    public abstract DateTime bucket();
    public abstract long totalTransactionCount();
    public abstract long successfulTransactionCount();
    public abstract long failedTransactionCount();

    public static DHCPStatisticsBucket create(DateTime bucket, long totalTransactionCount, long successfulTransactionCount, long failedTransactionCount) {
        return builder()
                .bucket(bucket)
                .totalTransactionCount(totalTransactionCount)
                .successfulTransactionCount(successfulTransactionCount)
                .failedTransactionCount(failedTransactionCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DHCPStatisticsBucket.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder totalTransactionCount(long totalTransactionCount);

        public abstract Builder successfulTransactionCount(long successfulTransactionCount);

        public abstract Builder failedTransactionCount(long failedTransactionCount);

        public abstract DHCPStatisticsBucket build();
    }
}
