package app.nzyme.core.connect.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConnectNodeLogCountReport {

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

    @JsonProperty("fatal")
    public abstract long fatal();

    public static ConnectNodeLogCountReport create(long trace, long debug, long info, long warn, long error, long fatal) {
        return builder()
                .trace(trace)
                .debug(debug)
                .info(info)
                .warn(warn)
                .error(error)
                .fatal(fatal)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectNodeLogCountReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder trace(long trace);

        public abstract Builder debug(long debug);

        public abstract Builder info(long info);

        public abstract Builder warn(long warn);

        public abstract Builder error(long error);

        public abstract Builder fatal(long fatal);

        public abstract ConnectNodeLogCountReport build();
    }
}
