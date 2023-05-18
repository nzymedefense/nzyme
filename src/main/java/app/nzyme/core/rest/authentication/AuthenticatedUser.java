package app.nzyme.core.rest.authentication;

import org.joda.time.DateTime;

import java.security.Principal;
import java.util.UUID;

public class AuthenticatedUser implements Principal {

    private final UUID userId;
    private final String sessionId;
    private final String email;

    private final DateTime sessionCreatedAt;

    private final UUID organizationId;
    private final UUID tenantId;

    private final boolean isOrganizationAdministrator;
    private final boolean isSuperAdministrator;

    public AuthenticatedUser(UUID userId,
                             String sessionId,
                             String email,
                             DateTime sessionCreatedAt,
                             UUID organizationId,
                             UUID tenantId,
                             final boolean isOrganizationAdministrator,
                             boolean isSuperAdministrator) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.email = email;
        this.sessionCreatedAt = sessionCreatedAt;
        this.organizationId = organizationId;
        this.tenantId = tenantId;
        this.isOrganizationAdministrator = isOrganizationAdministrator;
        this.isSuperAdministrator = isSuperAdministrator;
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

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public boolean isOrganizationAdministrator() {
        return isOrganizationAdministrator;
    }

    public boolean isSuperAdministrator() {
        return isSuperAdministrator;
    }

    @Override
    public String getName() {
        return getEmail();
    }

}
