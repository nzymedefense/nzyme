package app.nzyme.core.rest.resources.monitors;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.monitors.MonitorType;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.UUID;

@Path("/api/monitors")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class MonitorsResource extends UserAuthenticatedResource {

    @Inject
    private NzymeNode nzyme;

    @POST
    @Path("/type/{monitor_type}")
    public Response create(@Context SecurityContext sc,
                           @QueryParam("organization_id") @NotNull UUID organizationId,
                           @QueryParam("tenant_id") @NotNull UUID tenantId,
                           @PathParam("monitor_type") MonitorType monitorType) {
        AuthenticatedUser user = getAuthenticatedUser(sc);

        // Check permissions.
        if (!user.isSuperAdministrator()
                && !(user.isOrganizationAdministrator() && organizationId.equals(user.getOrganizationId()))) {
            // User is not a super admin or admin of the passed org. Check user permissions.
            List<String> userPermissions = nzyme.getAuthenticationService().findPermissionsOfUser(user.getUserId());
            String requiredPermission;
            switch (monitorType) {
                case DOT11_BSSID:
                case DOT11_CLIENT:
                    requiredPermission = "dot11_monitoring_manage";
                    break;
                default:
                    return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (!userPermissions.contains(requiredPermission)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        //nzyme.getMonitors().createMonitor(monitorType, );

        return Response.ok().build();
    }

}
