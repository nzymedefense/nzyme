package app.nzyme.core.rest.responses.dot11.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class DisconnectedClientListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("clients")
    public abstract List<DisconnectedClientDetailsResponse> clients();

    public static DisconnectedClientListResponse create(long total, List<DisconnectedClientDetailsResponse> clients) {
        return builder()
                .total(total)
                .clients(clients)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DisconnectedClientListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder clients(List<DisconnectedClientDetailsResponse> clients);

        public abstract DisconnectedClientListResponse build();
    }
}
