package app.nzyme.core.security.sessions.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class SessionEntryWithUserDetails {

    public abstract Long id();
    public abstract String sessionId();
    public abstract long userId();
    public abstract boolean isSuperadmin();
    public abstract boolean isOrgadmin();
    public abstract String userEmail();
    public abstract String userName();
    public abstract String remoteIp();
    public abstract DateTime createdAt();

    @Nullable
    public abstract Long organizationId();

    @Nullable
    public abstract Long tenantId();

    @Nullable
    public abstract DateTime lastActivity();

    public static SessionEntryWithUserDetails create(Long id, String sessionId, long userId, boolean isSuperadmin, boolean isOrgadmin, String userEmail, String userName, String remoteIp, DateTime createdAt, Long organizationId, Long tenantId, DateTime lastActivity) {
        return builder()
                .id(id)
                .sessionId(sessionId)
                .userId(userId)
                .isSuperadmin(isSuperadmin)
                .isOrgadmin(isOrgadmin)
                .userEmail(userEmail)
                .userName(userName)
                .remoteIp(remoteIp)
                .createdAt(createdAt)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .lastActivity(lastActivity)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SessionEntryWithUserDetails.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder sessionId(String sessionId);

        public abstract Builder userId(long userId);

        public abstract Builder isSuperadmin(boolean isSuperadmin);

        public abstract Builder isOrgadmin(boolean isOrgadmin);

        public abstract Builder userEmail(String userEmail);

        public abstract Builder userName(String userName);

        public abstract Builder remoteIp(String remoteIp);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder organizationId(Long organizationId);

        public abstract Builder tenantId(Long tenantId);

        public abstract Builder lastActivity(DateTime lastActivity);

        public abstract SessionEntryWithUserDetails build();
    }
}
