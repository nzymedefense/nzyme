package app.nzyme.core.rest.responses.ethernet.dns;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class DNSEntropyLogResponse {

    @JsonProperty("query")
    public abstract DNSLogDataResponse query();

    @JsonProperty("responses")
    public abstract List<DNSLogDataResponse> responses();

    @JsonProperty("entropy")
    public abstract float entropy();

    @JsonProperty("entropy_mean")
    public abstract float entropyMean();

    @JsonProperty("zscore")
    public abstract float zscore();

    public static DNSEntropyLogResponse create(DNSLogDataResponse query, List<DNSLogDataResponse> responses, float entropy, float entropyMean, float zscore) {
        return builder()
                .query(query)
                .responses(responses)
                .entropy(entropy)
                .entropyMean(entropyMean)
                .zscore(zscore)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSEntropyLogResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder query(DNSLogDataResponse query);

        public abstract Builder responses(List<DNSLogDataResponse> responses);

        public abstract Builder entropy(float entropy);

        public abstract Builder entropyMean(float entropyMean);

        public abstract Builder zscore(float zscore);

        public abstract DNSEntropyLogResponse build();
    }
}
