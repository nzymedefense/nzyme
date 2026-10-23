package app.nzyme.core.rest.responses.ethernet.recon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ReconScanAnsweredPortResponse {

    @JsonProperty("port")
    public abstract int port();

    @JsonProperty("probe_count")
    public abstract long probeCount();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static ReconScanAnsweredPortResponse create(int port, long probeCount, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .port(port)
                .probeCount(probeCount)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconScanAnsweredPortResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder port(int port);
        public abstract Builder probeCount(long probeCount);
        public abstract Builder firstSeen(DateTime firstSeen);
        public abstract Builder lastSeen(DateTime lastSeen);
        public abstract ReconScanAnsweredPortResponse build();
    }
}
