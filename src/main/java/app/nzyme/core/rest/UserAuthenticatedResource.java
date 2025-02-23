package app.nzyme.core.rest;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.monitoring.MonitoredSSID;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class UserAuthenticatedResource extends RestResource {

    @Inject
    private NzymeNode nzyme;

    private static final Logger LOG = LogManager.getLogger(UserAuthenticatedResource.class);

    protected AuthenticatedUser getAuthenticatedUser(SecurityContext sc) {
        return (AuthenticatedUser) sc.getUserPrincipal();
    }

    protected boolean passedTenantDataAccessible(SecurityContext sc, UUID organizationId, UUID tenantId) {
        AuthenticatedUser user = getAuthenticatedUser(sc);

        // Super admin?
        if (user.isSuperAdministrator()) {
            return true;
        }

        // Org admin?
        if (user.isOrganizationAdministrator() && user.getOrganizationId().equals(organizationId)) {
            return true;
        }

        // Tenant user.
        Optional<OrganizationEntry> dbOrg = nzyme.getAuthenticationService().findOrganization(organizationId);
        if (dbOrg.isEmpty()) {
            return false;
        }

        Optional<TenantEntry> dbTenant = nzyme.getAuthenticationService().findTenant(tenantId);
        if (dbTenant.isEmpty()) {
            return false;
        }

        // Check if tenant is part of org.
        if (!dbTenant.get().organizationUuid().equals(organizationId)) {
            return false;
        }

        return user.getOrganizationId().equals(organizationId)
                && user.getTenantId().equals(tenantId);
    }

    protected boolean passedMonitoredNetworkAccessible(AuthenticatedUser user, MonitoredSSID ssid) {
        if (user.isSuperAdministrator()) {
            return true;
        }

        if (user.isOrganizationAdministrator() && ssid.organizationId().equals(user.getOrganizationId())) {
            return true;
        }

        if (ssid.organizationId().equals(user.getOrganizationId()) && ssid.tenantId().equals(user.getTenantId())) {
            return true;
        }

        return false;
    }

}
