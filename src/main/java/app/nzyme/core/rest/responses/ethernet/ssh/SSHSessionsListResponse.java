package app.nzyme.core.rest.responses.ethernet.ssh;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class SSHSessionsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("sessions")
    public abstract List<SSHSessionDetailsResponse> sessions();

    public static SSHSessionsListResponse create(long total, List<SSHSessionDetailsResponse> sessions) {
        return builder()
                .total(total)
                .sessions(sessions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSHSessionsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder sessions(List<SSHSessionDetailsResponse> sessions);

        public abstract SSHSessionsListResponse build();
    }
}
