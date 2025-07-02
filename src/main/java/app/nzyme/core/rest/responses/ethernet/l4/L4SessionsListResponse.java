package app.nzyme.core.rest.responses.ethernet.l4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class L4SessionsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("sessions")
    public abstract List<L4SessionDetailsResponse> sessions();

    public static L4SessionsListResponse create(long total, List<L4SessionDetailsResponse> sessions) {
        return builder()
                .total(total)
                .sessions(sessions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_L4SessionsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder sessions(List<L4SessionDetailsResponse> sessions);

        public abstract L4SessionsListResponse build();
    }
}
