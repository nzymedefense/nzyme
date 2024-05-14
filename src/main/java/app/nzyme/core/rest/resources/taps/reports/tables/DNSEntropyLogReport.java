package app.nzyme.core.rest.resources.taps.reports.tables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DNSEntropyLogReport {

    public abstract Integer transactionId();
    public abstract Float entropy();
    public abstract Float zScore();
    public abstract Float entropyMean();

    @JsonCreator
    public static DNSEntropyLogReport create(@JsonProperty("transaction_id") Integer transactionId,
                                             @JsonProperty("entropy") Float entropy,
                                             @JsonProperty("zscore") Float zScore,
                                             @JsonProperty("entropy_mean") Float entropyMean) {
        return builder()
                .transactionId(transactionId)
                .entropy(entropy)
                .zScore(zScore)
                .entropyMean(entropyMean)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSEntropyLogReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder transactionId(Integer transactionId);

        public abstract Builder entropy(Float entropy);

        public abstract Builder zScore(Float zScore);

        public abstract Builder entropyMean(Float entropyMean);

        public abstract DNSEntropyLogReport build();
    }

}
