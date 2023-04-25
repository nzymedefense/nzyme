package app.nzyme.core.rest.authentication;

import org.joda.time.DateTime;

import java.security.Principal;

public class AuthenticatedUser implements Principal {

    private final long userId;
    private final String sessionId;
    private final String email;

    private final DateTime sessionCreatedAt;

    private final long organizationId;
    private final long tenantId;

    public AuthenticatedUser(long userId, String sessionId, String email, DateTime sessionCreatedAt, long organizationId, long tenantId) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.email = email;
        this.sessionCreatedAt = sessionCreatedAt;
        this.organizationId = organizationId;
        this.tenantId = tenantId;
    }

    public long getUserId() {
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

    public long getOrganizationId() {
        return organizationId;
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public String getName() {
        return getEmail();
    }

}
