package app.nzyme.core.rest.responses.authentication.mgmt;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SuperadminSettingsResponse {

    @JsonProperty("session_timeout_minutes")
    public abstract ConfigurationEntryResponse sessionTimeoutMinutes();

    @JsonProperty("session_inactivity_timeout_minutes")
    public abstract ConfigurationEntryResponse sessionInactivityTimeoutMinutes();

    @JsonProperty("mfa_timeout_minutes")
    public abstract ConfigurationEntryResponse mfaTimeoutMinutes();

    public static SuperadminSettingsResponse create(ConfigurationEntryResponse sessionTimeoutMinutes, ConfigurationEntryResponse sessionInactivityTimeoutMinutes, ConfigurationEntryResponse mfaTimeoutMinutes) {
        return builder()
                .sessionTimeoutMinutes(sessionTimeoutMinutes)
                .sessionInactivityTimeoutMinutes(sessionInactivityTimeoutMinutes)
                .mfaTimeoutMinutes(mfaTimeoutMinutes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SuperadminSettingsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sessionTimeoutMinutes(ConfigurationEntryResponse sessionTimeoutMinutes);

        public abstract Builder sessionInactivityTimeoutMinutes(ConfigurationEntryResponse sessionInactivityTimeoutMinutes);

        public abstract Builder mfaTimeoutMinutes(ConfigurationEntryResponse mfaTimeoutMinutes);

        public abstract SuperadminSettingsResponse build();
    }

}
