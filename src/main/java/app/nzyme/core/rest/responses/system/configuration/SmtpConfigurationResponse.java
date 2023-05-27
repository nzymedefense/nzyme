package app.nzyme.core.rest.responses.system.configuration;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import app.nzyme.plugin.rest.configuration.EncryptedConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SmtpConfigurationResponse {

    @JsonProperty("smtp_transport_strategy")
    public abstract ConfigurationEntryResponse transportStrategy();

    @JsonProperty("smtp_host")
    public abstract ConfigurationEntryResponse host();

    @JsonProperty("smtp_port")
    public abstract ConfigurationEntryResponse port();

    @JsonProperty("smtp_username")
    public abstract ConfigurationEntryResponse username();

    @JsonProperty("smtp_password")
    public abstract EncryptedConfigurationEntryResponse password();

    @JsonProperty("smtp_from_address")
    public abstract ConfigurationEntryResponse from();

    public static SmtpConfigurationResponse create(ConfigurationEntryResponse transportStrategy, ConfigurationEntryResponse host, ConfigurationEntryResponse port, ConfigurationEntryResponse username, EncryptedConfigurationEntryResponse password, ConfigurationEntryResponse from) {
        return builder()
                .transportStrategy(transportStrategy)
                .host(host)
                .port(port)
                .username(username)
                .password(password)
                .from(from)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SmtpConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder transportStrategy(ConfigurationEntryResponse transportStrategy);

        public abstract Builder host(ConfigurationEntryResponse host);

        public abstract Builder port(ConfigurationEntryResponse port);

        public abstract Builder username(ConfigurationEntryResponse username);

        public abstract Builder password(EncryptedConfigurationEntryResponse password);

        public abstract Builder from(ConfigurationEntryResponse from);

        public abstract SmtpConfigurationResponse build();
    }
}
