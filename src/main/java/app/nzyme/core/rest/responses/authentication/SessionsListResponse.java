package app.nzyme.core.rest.responses.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class SessionsListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("sessions")
    public abstract List<SessionDetailsResponse> sessions();

    public static SessionsListResponse create(long count, List<SessionDetailsResponse> sessions) {
        return builder()
                .count(count)
                .sessions(sessions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SessionsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder sessions(List<SessionDetailsResponse> sessions);

        public abstract SessionsListResponse build();
    }
}
