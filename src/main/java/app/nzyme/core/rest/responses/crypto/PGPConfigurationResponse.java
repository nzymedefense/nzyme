package app.nzyme.core.rest.responses.crypto;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PGPConfigurationResponse {

    @JsonProperty("pgp_sync_enabled")
    public abstract ConfigurationEntryResponse pgpSyncEnabled();

    public static PGPConfigurationResponse create(ConfigurationEntryResponse pgpSyncEnabled) {
        return builder()
                .pgpSyncEnabled(pgpSyncEnabled)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PGPConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder pgpSyncEnabled(ConfigurationEntryResponse pgpSyncEnabled);

        public abstract PGPConfigurationResponse build();
    }

}
