package app.nzyme.core.rest.responses.dot11.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ConnectedClientListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("clients")
    public abstract List<ConnectedClientDetailsResponse> clients();

    public static ConnectedClientListResponse create(long total, List<ConnectedClientDetailsResponse> clients) {
        return builder()
                .total(total)
                .clients(clients)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectedClientListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder clients(List<ConnectedClientDetailsResponse> clients);

        public abstract ConnectedClientListResponse build();
    }
}
