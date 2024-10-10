package app.nzyme.core.rest.responses.dot11.monitoring.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class KnownClientsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("clients")
    public abstract List<KnownClientDetailsResponse> clients();

    public static KnownClientsListResponse create(long total, List<KnownClientDetailsResponse> clients) {
        return builder()
                .total(total)
                .clients(clients)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_KnownClientsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder clients(List<KnownClientDetailsResponse> clients);

        public abstract KnownClientsListResponse build();
    }

}
