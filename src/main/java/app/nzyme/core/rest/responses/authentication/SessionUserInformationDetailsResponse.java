package app.nzyme.core.rest.responses.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

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

    @Nullable
    @JsonProperty("organization_id")
    public abstract UUID organizationId();

    @Nullable
    @JsonProperty("tenant_id")
    public abstract UUID tenantId();

    @JsonProperty("feature_permissions")
    public abstract List<String> featurePermissions();

    @JsonProperty("subsystems")
    public abstract List<String> subsystems();

    @JsonProperty("has_mfa_disabled")
    public abstract boolean hasMfaDisabled();

    public static SessionUserInformationDetailsResponse create(UUID id, String email, String name, boolean isSuperAdmin, boolean isOrgAdmin, UUID organizationId, UUID tenantId, List<String> featurePermissions, List<String> subsystems, boolean hasMfaDisabled) {
        return builder()
                .id(id)
                .email(email)
                .name(name)
                .isSuperAdmin(isSuperAdmin)
                .isOrgAdmin(isOrgAdmin)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .featurePermissions(featurePermissions)
                .subsystems(subsystems)
                .hasMfaDisabled(hasMfaDisabled)
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

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder featurePermissions(List<String> featurePermissions);

        public abstract Builder subsystems(List<String> subsystems);

        public abstract Builder hasMfaDisabled(boolean hasMfaDisabled);

        public abstract SessionUserInformationDetailsResponse build();
    }
}
