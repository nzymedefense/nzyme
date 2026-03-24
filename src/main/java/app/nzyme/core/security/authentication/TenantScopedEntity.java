package app.nzyme.core.security.authentication;

import java.util.UUID;

public interface TenantScopedEntity {

    UUID organizationId();
    UUID tenantId();

}
