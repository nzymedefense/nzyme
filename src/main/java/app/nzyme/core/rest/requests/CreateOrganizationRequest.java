package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CreateOrganizationRequest {

    public abstract String name();
    public abstract String description();

    public abstract int sessionTimeoutMinutes();
    public abstract int sessionInactivityTimeoutMinutes();
    public abstract int mfaTimeoutMinutes();

    @JsonCreator
    public static CreateOrganizationRequest create(@JsonProperty("name") String name,
                                                   @JsonProperty("description") String description,
                                                   @JsonProperty("session_timeout_minutes") int sessionTimeoutMinutes,
                                                   @JsonProperty("session_inactivity_timeout_minutes") int sessionInactivityTimeoutMinutes,
                                                   @JsonProperty("mfa_timeout_minutes") int mfaTimeoutMinutes) {
        return builder()
                .name(name)
                .description(description)
                .sessionTimeoutMinutes(sessionTimeoutMinutes)
                .sessionInactivityTimeoutMinutes(sessionInactivityTimeoutMinutes)
                .mfaTimeoutMinutes(mfaTimeoutMinutes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateOrganizationRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder sessionTimeoutMinutes(int sessionTimeoutMinutes);

        public abstract Builder sessionInactivityTimeoutMinutes(int sessionInactivityTimeoutMinutes);

        public abstract Builder mfaTimeoutMinutes(int mfaTimeoutMinutes);

        public abstract CreateOrganizationRequest build();
    }
}
