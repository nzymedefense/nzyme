package app.nzyme.core.rest.responses.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class SessionUserInformationDetailsResponse {

    @JsonProperty("id")
    public abstract UUID id();

    @JsonProperty("email")
    public abstract String email();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("is_superadmin")
    public abstract boolean isSuperAdmin();

    @JsonProperty("is_orgadmin")
    public abstract boolean isOrgAdmin();

    @JsonProperty("feature_permissions")
    public abstract List<String> featurePermissions();

    public static SessionUserInformationDetailsResponse create(UUID id, String email, String name, boolean isSuperAdmin, boolean isOrgAdmin, List<String> featurePermissions) {
        return builder()
                .id(id)
                .email(email)
                .name(name)
                .isSuperAdmin(isSuperAdmin)
                .isOrgAdmin(isOrgAdmin)
                .featurePermissions(featurePermissions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SessionUserInformationDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder email(String email);

        public abstract Builder name(String name);

        public abstract Builder isSuperAdmin(boolean isSuperAdmin);

        public abstract Builder isOrgAdmin(boolean isOrgAdmin);

        public abstract Builder featurePermissions(List<String> featurePermissions);

        public abstract SessionUserInformationDetailsResponse build();
    }
}
