package app.nzyme.core.rest.authentication;

import java.security.Principal;
import java.util.UUID;

public class AuthenticatedTap implements Principal {

    private final UUID uuid;
    private final String name;

    private final long organizationId;
    private final long tenantId;

    public AuthenticatedTap(UUID uuid, String name, long organizationId, long tenantId) {
        this.uuid = uuid;
        this.name = name;
        this.organizationId = organizationId;
        this.tenantId = tenantId;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public long getTenantId() {
        return tenantId;
    }

}
