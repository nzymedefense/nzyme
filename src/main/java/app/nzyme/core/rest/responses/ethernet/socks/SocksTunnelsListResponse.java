package app.nzyme.core.rest.responses.ethernet.socks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class SocksTunnelsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("tunnels")
    public abstract List<SocksTunnelDetailsResponse> tunnels();

    public static SocksTunnelsListResponse create(long total, List<SocksTunnelDetailsResponse> tunnels) {
        return builder()
                .total(total)
                .tunnels(tunnels)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SocksTunnelsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder tunnels(List<SocksTunnelDetailsResponse> tunnels);

        public abstract SocksTunnelsListResponse build();
    }
}
