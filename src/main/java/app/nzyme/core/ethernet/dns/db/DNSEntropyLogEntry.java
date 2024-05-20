package app.nzyme.core.ethernet.dns.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class DNSEntropyLogEntry {

    public abstract long id();
    public abstract int transactionId();
    public abstract float entropy();
    public abstract float entropyMean();
    public abstract float zscore();
    public abstract DateTime timestamp();
    public abstract DateTime createdAt();
    public abstract UUID tapUUID();

    public static DNSEntropyLogEntry create(long id, int transactionId, float entropy, float entropyMean, float zscore, DateTime timestamp, DateTime createdAt, UUID tapUUID) {
        return builder()
                .id(id)
                .transactionId(transactionId)
                .entropy(entropy)
                .entropyMean(entropyMean)
                .zscore(zscore)
                .timestamp(timestamp)
                .createdAt(createdAt)
                .tapUUID(tapUUID)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSEntropyLogEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder transactionId(int transactionId);

        public abstract Builder entropy(float entropy);

        public abstract Builder entropyMean(float entropyMean);

        public abstract Builder zscore(float zscore);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder tapUUID(UUID tapUUID);

        public abstract DNSEntropyLogEntry build();
    }
}
