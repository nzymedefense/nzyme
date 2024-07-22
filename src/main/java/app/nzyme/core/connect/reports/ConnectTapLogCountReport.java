package app.nzyme.core.connect.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConnectTapLogCountReport {

    @JsonProperty("trace")
    public abstract long trace();

    @JsonProperty("debug")
    public abstract long debug();

    @JsonProperty("info")
    public abstract long info();

    @JsonProperty("warn")
    public abstract long warn();

    @JsonProperty("error")
    public abstract long error();

    public static ConnectTapLogCountReport create(long trace, long debug, long info, long warn, long error) {
        return builder()
                .trace(trace)
                .debug(debug)
                .info(info)
                .warn(warn)
                .error(error)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectTapLogCountReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder trace(long trace);

        public abstract Builder debug(long debug);

        public abstract Builder info(long info);

        public abstract Builder warn(long warn);

        public abstract Builder error(long error);

        public abstract ConnectTapLogCountReport build();
    }
}
