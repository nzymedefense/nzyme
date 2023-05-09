package app.nzyme.core.rest.responses.integrations.geoip.providers.ipinfofree;

import app.nzyme.plugin.rest.configuration.EncryptedConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class IpInfoFreeConfigurationResponse {

    @JsonProperty("token")
    public abstract EncryptedConfigurationEntryResponse token();

    public static IpInfoFreeConfigurationResponse create(EncryptedConfigurationEntryResponse token) {
        return builder()
                .token(token)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_IpInfoFreeConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder token(EncryptedConfigurationEntryResponse token);

        public abstract IpInfoFreeConfigurationResponse build();
    }
}
