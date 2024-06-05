package app.nzyme.core.rest.responses.ethernet.dns;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class DNSLogEntryResponse {

    @JsonProperty("query")
    public abstract DNSLogDataResponse query();

    @JsonProperty("responses")
    public abstract List<DNSLogDataResponse> responses();

    public static DNSLogEntryResponse create(DNSLogDataResponse query, List<DNSLogDataResponse> responses) {
        return builder()
                .query(query)
                .responses(responses)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSLogEntryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder query(DNSLogDataResponse query);

        public abstract Builder responses(List<DNSLogDataResponse> responses);

        public abstract DNSLogEntryResponse build();
    }
}
