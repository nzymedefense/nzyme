package app.nzyme.core.rest.responses.ethernet.dns;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class DNSEntropyLogListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("logs")
    public abstract List<DNSEntropyLogResponse> logs();

    public static DNSEntropyLogListResponse create(long total, List<DNSEntropyLogResponse> logs) {
        return builder()
                .total(total)
                .logs(logs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSEntropyLogListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder logs(List<DNSEntropyLogResponse> logs);

        public abstract DNSEntropyLogListResponse build();
    }
}
