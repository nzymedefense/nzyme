package app.nzyme.core.rest.responses.ethernet.dns;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class DNSLogListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("logs")
    public abstract List<DNSLogEntryResponse> logs();

    public static DNSLogListResponse create(long total, List<DNSLogEntryResponse> logs) {
        return builder()
                .total(total)
                .logs(logs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSLogListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder logs(List<DNSLogEntryResponse> logs);

        public abstract DNSLogListResponse build();
    }
}
