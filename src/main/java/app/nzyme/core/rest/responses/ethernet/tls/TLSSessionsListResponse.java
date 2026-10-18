package app.nzyme.core.rest.responses.ethernet.tls;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TLSSessionsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("sessions")
    public abstract List<TLSSessionDetailsResponse> sessions();

    public static TLSSessionsListResponse create(long total, List<TLSSessionDetailsResponse> sessions) {
        return builder()
                .total(total)
                .sessions(sessions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSSessionsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder sessions(List<TLSSessionDetailsResponse> sessions);

        public abstract TLSSessionsListResponse build();
    }
}
