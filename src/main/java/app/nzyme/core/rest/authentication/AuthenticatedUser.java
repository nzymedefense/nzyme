package app.nzyme.core.rest.authentication;

import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.security.Principal;
import java.util.UUID;

public class AuthenticatedUser implements Principal {

    private final UUID userId;
    private final String sessionId;
    private final String email;

    private final DateTime sessionCreatedAt;

    @Nullable
    private final UUID organizationId;

    @Nullable
    private final UUID tenantId;

    private final boolean isOrganizationAdministrator;
    private final boolean isSuperAdministrator;

    public boolean accessAllTenantTaps;

    public AuthenticatedUser(UUID userId,
                             String sessionId,
                             String email,
                             DateTime sessionCreatedAt,
                             @Nullable UUID organizationId,
                             @Nullable UUID tenantId,
                             final boolean isOrganizationAdministrator,
                             boolean isSuperAdministrator,
                             boolean accessAllTenantTaps) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.email = email;
        this.sessionCreatedAt = sessionCreatedAt;
        this.organizationId = organizationId;
        this.tenantId = tenantId;
        this.isOrganizationAdministrator = isOrganizationAdministrator;
        this.isSuperAdministrator = isSuperAdministrator;
        this.accessAllTenantTaps = accessAllTenantTaps;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getEmail() {
        return email;
    }

    public DateTime getSessionCreatedAt() {
        return sessionCreatedAt;
    }

    @Nullable
    public UUID getOrganizationId() {
        return organizationId;
    }

    @Nullable
    public UUID getTenantId() {
        return tenantId;
    }

    public boolean isOrganizationAdministrator() {
        return isOrganizationAdministrator;
    }

    public boolean isSuperAdministrator() {
        return isSuperAdministrator;
    }

    public boolean isAccessAllTenantTaps() {
        return accessAllTenantTaps;
    }

    @Override
    public String getName() {
        return getEmail();
    }

}
