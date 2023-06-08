package app.nzyme.core.security.authentication.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class UserEntry {

    public abstract UUID uuid();

    @Nullable
    public abstract UUID organizationId();

    @Nullable
    public abstract UUID tenantId();

    public abstract String passwordHash();
    public abstract String passwordSalt();

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

    @Nullable
    public abstract String lastRemoteIp();

    @Nullable
    public abstract String lastGeoCity();

    @Nullable
    public abstract String lastGeoCountry();

    @Nullable
    public abstract String lastGeoAsn();

    public abstract boolean accessAllTenantTaps();

    public abstract long failedLoginCount();

    public abstract boolean isLoginThrottled();

    public static UserEntry create(UUID uuid, UUID organizationId, UUID tenantId, String passwordHash, String passwordSalt, String email, String name, boolean isOrganizationAdmin, boolean isSuperAdmin, String totpSecret, boolean mfaComplete, String mfaRecoveryCodes, DateTime updatedAt, DateTime createdAt, DateTime lastActivity, String lastRemoteIp, String lastGeoCity, String lastGeoCountry, String lastGeoAsn, boolean accessAllTenantTaps, long failedLoginCount, boolean isLoginThrottled) {
        return builder()
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .passwordHash(passwordHash)
                .passwordSalt(passwordSalt)
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
                .lastRemoteIp(lastRemoteIp)
                .lastGeoCity(lastGeoCity)
                .lastGeoCountry(lastGeoCountry)
                .lastGeoAsn(lastGeoAsn)
                .accessAllTenantTaps(accessAllTenantTaps)
                .failedLoginCount(failedLoginCount)
                .isLoginThrottled(isLoginThrottled)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UserEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder passwordHash(String passwordHash);

        public abstract Builder passwordSalt(String passwordSalt);

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

        public abstract Builder lastRemoteIp(String lastRemoteIp);

        public abstract Builder lastGeoCity(String lastGeoCity);

        public abstract Builder lastGeoCountry(String lastGeoCountry);

        public abstract Builder lastGeoAsn(String lastGeoAsn);

        public abstract Builder accessAllTenantTaps(boolean accessAllTenantTaps);

        public abstract Builder failedLoginCount(long failedLoginCount);

        public abstract Builder isLoginThrottled(boolean isLoginThrottled);

        public abstract UserEntry build();
    }
}
