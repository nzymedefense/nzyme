package app.nzyme.core.rest.responses.connect;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import app.nzyme.plugin.rest.configuration.EncryptedConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConnectConfigurationResponse {

    @JsonProperty("connect_enabled")
    public abstract ConfigurationEntryResponse connectEnabled();

    @JsonProperty("connect_api_key")
    public abstract EncryptedConfigurationEntryResponse connectApiKey();

    public static ConnectConfigurationResponse create(ConfigurationEntryResponse connectEnabled, EncryptedConfigurationEntryResponse connectApiKey) {
        return builder()
                .connectEnabled(connectEnabled)
                .connectApiKey(connectApiKey)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder connectEnabled(ConfigurationEntryResponse connectEnabled);

        public abstract Builder connectApiKey(EncryptedConfigurationEntryResponse connectApiKey);

        public abstract ConnectConfigurationResponse build();
    }

}
