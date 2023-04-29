package app.nzyme.core.security.authentication.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class UserEntry {

    public abstract long id();

    @Nullable
    public abstract Long organizationId();

    @Nullable
    public abstract Long tenantId();

    public abstract String passwordHash();
    public abstract String passwordSalt();

    @Nullable
    public abstract Long roleId();

    public abstract String email();
    public abstract String name();

    public abstract boolean isOrganizationAdmin();
    public abstract boolean isSuperAdmin();

    @Nullable
    public abstract String totpSecret();

    public abstract boolean mfaComplete();

    @Nullable
    public abstract String mfaRecoveryCodes();

    public abstract DateTime updatedAt();
    public abstract DateTime createdAt();

    @Nullable
    public abstract DateTime lastActivity();

    public static UserEntry create(long id, Long organizationId, Long tenantId, String passwordHash, String passwordSalt, Long roleId, String email, String name, boolean isOrganizationAdmin, boolean isSuperAdmin, String totpSecret, boolean mfaComplete, String mfaRecoveryCodes, DateTime updatedAt, DateTime createdAt, DateTime lastActivity) {
        return builder()
                .id(id)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .passwordHash(passwordHash)
                .passwordSalt(passwordSalt)
                .roleId(roleId)
                .email(email)
                .name(name)
                .isOrganizationAdmin(isOrganizationAdmin)
                .isSuperAdmin(isSuperAdmin)
                .totpSecret(totpSecret)
                .mfaComplete(mfaComplete)
                .mfaRecoveryCodes(mfaRecoveryCodes)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .lastActivity(lastActivity)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UserEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder organizationId(Long organizationId);

        public abstract Builder tenantId(Long tenantId);

        public abstract Builder passwordHash(String passwordHash);

        public abstract Builder passwordSalt(String passwordSalt);

        public abstract Builder roleId(Long roleId);

        public abstract Builder email(String email);

        public abstract Builder name(String name);

        public abstract Builder isOrganizationAdmin(boolean isOrganizationAdmin);

        public abstract Builder isSuperAdmin(boolean isSuperAdmin);

        public abstract Builder totpSecret(String totpSecret);

        public abstract Builder mfaComplete(boolean mfaComplete);

        public abstract Builder mfaRecoveryCodes(String mfaRecoveryCodes);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder lastActivity(DateTime lastActivity);

        public abstract UserEntry build();
    }
}
