package app.nzyme.core.rest.responses.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class SessionDetailsResponse {

    @JsonProperty("id")
    public abstract Long id();

    @JsonProperty("organization_id")
    @Nullable
    public abstract Long organizationId();

    @JsonProperty("tenant_id")
    @Nullable
    public abstract Long tenantId();

    @JsonProperty("user_id")
    public abstract long userId();

    @JsonProperty("user_email")
    public abstract String userEmail();

    @JsonProperty("user_name")
    public abstract String userName();

    @JsonProperty("is_superadmin")
    public abstract boolean isSuperadmin();

    @JsonProperty("is_orgadmin")
    public abstract boolean isOrgadmin();

    @JsonProperty("remote_ip")
    public abstract String remoteIp();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("last_activity")
    @Nullable
    public abstract DateTime lastActivity();

    @JsonProperty("mfa_valid")
    public abstract boolean mfaValid();

    @JsonProperty("mfa_requested_at")
    @Nullable
    public abstract DateTime mfaRequestedAt();

    public static SessionDetailsResponse create(Long id, Long organizationId, Long tenantId, long userId, String userEmail, String userName, boolean isSuperadmin, boolean isOrgadmin, String remoteIp, DateTime createdAt, DateTime lastActivity, boolean mfaValid, DateTime mfaRequestedAt) {
        return builder()
                .id(id)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .userId(userId)
                .userEmail(userEmail)
                .userName(userName)
                .isSuperadmin(isSuperadmin)
                .isOrgadmin(isOrgadmin)
                .remoteIp(remoteIp)
                .createdAt(createdAt)
                .lastActivity(lastActivity)
                .mfaValid(mfaValid)
                .mfaRequestedAt(mfaRequestedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SessionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder organizationId(Long organizationId);

        public abstract Builder tenantId(Long tenantId);

        public abstract Builder userId(long userId);

        public abstract Builder userEmail(String userEmail);

        public abstract Builder userName(String userName);

        public abstract Builder isSuperadmin(boolean isSuperadmin);

        public abstract Builder isOrgadmin(boolean isOrgadmin);

        public abstract Builder remoteIp(String remoteIp);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder lastActivity(DateTime lastActivity);

        public abstract Builder mfaValid(boolean mfaValid);

        public abstract Builder mfaRequestedAt(DateTime mfaRequestedAt);

        public abstract SessionDetailsResponse build();
    }
}
