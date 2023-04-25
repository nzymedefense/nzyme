package app.nzyme.core.security.authentication.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class UserEntry {

    public abstract long id();

    public abstract long organizationId();
    public abstract long tenantId();

    public abstract String passwordHash();
    public abstract String passwordSalt();

    @Nullable
    public abstract Long roleId();

    public abstract String email();
    public abstract String name();

    public abstract boolean isOrganizationAdmin();
    public abstract boolean isSuperAdmin();

    public abstract DateTime updatedAt();
    public abstract DateTime createdAt();

    public static UserEntry create(long id, long organizationId, long tenantId, String passwordHash, String passwordSalt, Long roleId, String email, String name, boolean isOrganizationAdmin, boolean isSuperAdmin, DateTime updatedAt, DateTime createdAt) {
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
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UserEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder organizationId(long organizationId);

        public abstract Builder tenantId(long tenantId);

        public abstract Builder passwordHash(String passwordHash);

        public abstract Builder passwordSalt(String passwordSalt);

        public abstract Builder roleId(Long roleId);

        public abstract Builder email(String email);

        public abstract Builder name(String name);

        public abstract Builder isOrganizationAdmin(boolean isOrganizationAdmin);

        public abstract Builder isSuperAdmin(boolean isSuperAdmin);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract UserEntry build();
    }
}
