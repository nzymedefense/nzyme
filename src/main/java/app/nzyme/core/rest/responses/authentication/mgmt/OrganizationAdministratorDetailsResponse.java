package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class OrganizationAdministratorDetailsResponse {

    @JsonProperty("user")
    public abstract UserDetailsResponse user();

    @JsonProperty("is_deletable")
    public abstract boolean isDeletable();

    public static OrganizationAdministratorDetailsResponse create(UserDetailsResponse user, boolean isDeletable) {
        return builder()
                .user(user)
                .isDeletable(isDeletable)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_OrganizationAdministratorDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder user(UserDetailsResponse user);

        public abstract Builder isDeletable(boolean isDeletable);

        public abstract OrganizationAdministratorDetailsResponse build();
    }
}
