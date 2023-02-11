package app.nzyme.core.rest.resources.taps.reports.tables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DNSEntropyLogReport {

    public abstract String logType();
    public abstract Float entropy();
    public abstract Float zScore();
    public abstract String value();
    public abstract DateTime timestamp();

    @JsonCreator
    public static DNSEntropyLogReport create(@JsonProperty("log_type") String logType,
                                             @JsonProperty("entropy") Float entropy,
                                             @JsonProperty("zscore") Float zScore,
                                             @JsonProperty("value") String value,
                                             @JsonProperty("timestamp") DateTime timestamp) {
        return builder()
                .logType(logType)
                .entropy(entropy)
                .zScore(zScore)
                .value(value)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSEntropyLogReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder logType(String logType);

        public abstract Builder entropy(Float entropy);

        public abstract Builder zScore(Float zScore);

        public abstract Builder value(String value);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract DNSEntropyLogReport build();
    }

}
