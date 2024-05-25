package app.nzyme.core.rest.resources.taps.reports.tables.socks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class SocksTunnelsReport {

    public abstract List<SocksTunnelReport> tunnels();

    @JsonCreator
    public static SocksTunnelsReport create(@JsonProperty("tunnels") List<SocksTunnelReport> tunnels) {
        return builder()
                .tunnels(tunnels)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SocksTunnelsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tunnels(List<SocksTunnelReport> tunnels);

        public abstract SocksTunnelsReport build();
    }
}