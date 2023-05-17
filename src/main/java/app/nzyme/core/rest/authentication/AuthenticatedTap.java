package app.nzyme.core.rest.authentication;

import java.security.Principal;
import java.util.UUID;

public class AuthenticatedTap implements Principal {

    private final UUID uuid;
    private final String name;

    private final UUID organizationId;
    private final UUID tenantId;

    public AuthenticatedTap(UUID uuid, String name, UUID organizationId, UUID tenantId) {
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

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

}
