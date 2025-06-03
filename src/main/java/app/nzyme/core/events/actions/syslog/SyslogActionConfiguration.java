package app.nzyme.core.events.actions.syslog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SyslogActionConfiguration {

    @JsonProperty("protocol")
    public abstract String protocol();

    @JsonProperty("syslog_hostname")
    public abstract String syslogHostname();

    @JsonProperty("host")
    public abstract String host();

    @JsonProperty("port")
    public abstract int port();

    @JsonCreator
    public static SyslogActionConfiguration create(@JsonProperty("protocol") String protocol,
                                                   @JsonProperty("syslog_hostname") String syslogHostname,
                                                   @JsonProperty("host") String host,
                                                   @JsonProperty("port") int port) {
        return builder()
                .protocol(protocol)
                .syslogHostname(syslogHostname)
                .host(host)
                .port(port)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SyslogActionConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder protocol(String protocol);

        public abstract Builder syslogHostname(String syslogHostname);

        public abstract Builder host(String host);

        public abstract Builder port(int port);

        public abstract SyslogActionConfiguration build();
    }
}
