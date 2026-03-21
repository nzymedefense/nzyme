package app.nzyme.core.rest.resources.monitors;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.monitors.MonitorType;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.CreateMonitorRequest;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
                           @Valid CreateMonitorRequest req,
                           @PathParam("monitor_type") MonitorType monitorType) {
        AuthenticatedUser user = getAuthenticatedUser(sc);

        List<UUID> tapUuids;
        if (req.taps().size() == 1 && req.taps().get(0).equals("*")) {
            // All taps selected.
            tapUuids = null;
        } else {
            tapUuids = Lists.newArrayList();
            List<UUID> userAccessibleTaps = nzyme.getTapManager().allTapUUIDsAccessibleByUser(user);
            for (String tapId : req.taps()) {
                UUID tapUuid = UUID.fromString(tapId);
                if (!userAccessibleTaps.contains(tapUuid)) {
                    return Response.status(Response.Status.FORBIDDEN).build();
                } else {
                    tapUuids.add(tapUuid);
                }
            }
        }

        // Check permissions.
        if (!user.isSuperAdministrator()
                && !(user.isOrganizationAdministrator() && req.organizationId().equals(user.getOrganizationId()))) {
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

        if (!passedTenantDataAccessible(sc, req.organizationId(), req.tenantId())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        nzyme.getMonitors().createMonitor(
                monitorType,
                req.name(),
                req.description(),
                tapUuids,
                req.triggerCondition(),
                req.interval(),
                parseFiltersQueryParameter(req.filters()),
                req.organizationId(),
                req.tenantId()
        );

        return Response.status(Response.Status.CREATED).build();
    }

}
