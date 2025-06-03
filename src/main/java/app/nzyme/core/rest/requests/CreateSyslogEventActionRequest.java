package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

@AutoValue
public abstract class CreateSyslogEventActionRequest {

    @Nullable
    public abstract UUID organizationId();

    @NotEmpty
    public abstract String name();

    @NotEmpty
    public abstract String description();

    @NotEmpty
    public abstract String protocol();

    @NotEmpty
    public abstract String syslogHostname();

    @NotEmpty
    public abstract String host();

    @Min(1)
    @Max(65535)
    public abstract int port();

    @JsonCreator
    public static CreateSyslogEventActionRequest create(@JsonProperty("organization_id") UUID organizationId,
                                                        @JsonProperty("name") String name,
                                                        @JsonProperty("description") String description,
                                                        @JsonProperty("protocol") String protocol,
                                                        @JsonProperty("syslog_hostname") String syslogHostname,
                                                        @JsonProperty("host") String host,
                                                        @JsonProperty("port") int port) {
        return builder()
                .organizationId(organizationId)
                .name(name)
                .description(description)
                .protocol(protocol)
                .syslogHostname(syslogHostname)
                .host(host)
                .port(port)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateSyslogEventActionRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder name(@NotEmpty String name);

        public abstract Builder description(@NotEmpty String description);

        public abstract Builder protocol(@NotEmpty String protocol);

        public abstract Builder syslogHostname(@NotEmpty String syslogHostname);

        public abstract Builder host(@NotEmpty String host);

        public abstract Builder port(@Min(1) @Max(65535) int port);

        public abstract CreateSyslogEventActionRequest build();
    }
}
