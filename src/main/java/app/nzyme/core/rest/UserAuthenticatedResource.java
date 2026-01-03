package app.nzyme.core.rest;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.monitoring.MonitoredSSID;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.SecurityContext;

import java.util.Optional;
import java.util.UUID;

public class UserAuthenticatedResource extends RestResource {

    @Inject
    private NzymeNode nzyme;

    protected AuthenticatedUser getAuthenticatedUser(SecurityContext sc) {
        return (AuthenticatedUser) sc.getUserPrincipal();
    }

    protected boolean passedTenantDataAccessible(SecurityContext sc,
                                                 @NotNull UUID organizationId,
                                                 @NotNull UUID tenantId) {
        if (organizationId == null || tenantId == null) {
            throw new IllegalArgumentException("Organization and Tenant ID must be set.");
        }

        AuthenticatedUser user = getAuthenticatedUser(sc);

        // Super admin?
        if (user.isSuperAdministrator()) {
            return true;
        }

        Optional<OrganizationEntry> dbOrg = nzyme.getAuthenticationService().findOrganization(organizationId);
        if (dbOrg.isEmpty()) {
            return false;
        }

        Optional<TenantEntry> dbTenant = nzyme.getAuthenticationService().findTenant(tenantId);
        if (dbTenant.isEmpty()) {
            return false;
        }

        // Is tenant part of organization?
        if (!dbTenant.get().organizationUuid().equals(organizationId)) {
            return false;
        }

        // Organization admin of the requested organization?
        if (user.isOrganizationAdministrator() && user.getOrganizationId().equals(organizationId)) {
            return true;
        }

        // User of tenant and organization that was requested?
        return user.getOrganizationId().equals(organizationId) && user.getTenantId().equals(tenantId);
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
