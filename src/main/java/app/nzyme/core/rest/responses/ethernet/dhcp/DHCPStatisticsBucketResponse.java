package app.nzyme.core.rest.responses.ethernet.dhcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DHCPStatisticsBucketResponse {

    @JsonProperty("total_transaction_count")
    public abstract long totalTransactionCount();

    @JsonProperty("successful_transaction_count")
    public abstract long successfulTransactionCount();

    @JsonProperty("failed_transaction_count")
    public abstract long failedTransactionCount();

    public static DHCPStatisticsBucketResponse create(long totalTransactionCount, long successfulTransactionCount, long failedTransactionCount) {
        return builder()
                .totalTransactionCount(totalTransactionCount)
                .successfulTransactionCount(successfulTransactionCount)
                .failedTransactionCount(failedTransactionCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DHCPStatisticsBucketResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder totalTransactionCount(long totalTransactionCount);

        public abstract Builder successfulTransactionCount(long successfulTransactionCount);

        public abstract Builder failedTransactionCount(long failedTransactionCount);

        public abstract DHCPStatisticsBucketResponse build();
    }
}
