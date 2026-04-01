package app.nzyme.core.rest.resources.monitors;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.monitors.MonitorType;
import app.nzyme.core.monitors.db.MonitorEntry;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.CreateMonitorRequest;
import app.nzyme.core.rest.requests.UpdateMonitorRequest;
import app.nzyme.core.rest.responses.monitors.MonitorDetailsResponse;
import app.nzyme.core.rest.responses.monitors.MonitorListResponse;
import app.nzyme.core.rest.responses.taps.TapHighLevelInformationDetailsResponse;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/monitors")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class MonitorsResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/show/{id}")
    public Response findOne(@Context SecurityContext sc,
                            @PathParam("id") UUID uuid) {
        AuthenticatedUser user = getAuthenticatedUser(sc);
        Optional<MonitorEntry> monitor = nzyme.getMonitors().find(uuid);

        if (monitor.isEmpty() || !entityAccessible(user, monitor.get())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<UUID> filteredTapUUIDs;
        boolean partialData;
        if (monitor.get().taps() == null) {
            filteredTapUUIDs = null;
            partialData = !user.accessAllTenantTaps;
        } else {
            filteredTapUUIDs = parseAndValidateTapIdsDirect(
                    user,
                    nzyme,
                    monitor.get().taps()
            );
            partialData = filteredTapUUIDs.size() != monitor.get().taps().size();
        }

        return Response.ok(MonitorDetailsResponse.create(
                monitor.get().uuid(),
                monitor.get().organizationId(),
                monitor.get().tenantId(),
                monitor.get().enabled(),
                monitor.get().type(),
                monitor.get().name(),
                monitor.get().description(),
                filteredTapUUIDs,
                monitor.get().triggerCondition(),
                monitor.get().interval(),
                monitor.get().filters(),
                monitor.get().alerted(),
                monitor.get().lastEvent(),
                monitor.get().createdAt(),
                monitor.get().updatedAt(),
                partialData
        )).build();
    }

    @GET
    @Path("/type/{monitor_type}")
    public Response findAll(@Context SecurityContext sc,
                            @PathParam("monitor_type") MonitorType monitorType,
                            @QueryParam("organization_id") @NotNull UUID organizationId,
                            @QueryParam("tenant_id") @NotNull UUID tenantId,
                            @QueryParam("limit") int limit,
                            @QueryParam("offset") int offset) {
        AuthenticatedUser user = getAuthenticatedUser(sc);

        if (limit > 200 || offset < 0) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        long total = nzyme.getMonitors().countAllMonitorsOfType(monitorType, organizationId, tenantId);

        List<MonitorDetailsResponse> monitors = Lists.newArrayList();
        for (MonitorEntry m : nzyme.getMonitors().findAllMonitorsOfType(monitorType, organizationId, tenantId, offset, limit)) {
            List<UUID> filteredTapUUIDs;
            boolean partialData;
            if (m.taps() == null) {
                filteredTapUUIDs = null;
                partialData = !user.accessAllTenantTaps;
            } else {
                filteredTapUUIDs = parseAndValidateTapIdsDirect(
                        user,
                        nzyme,
                        m.taps()
                );
                partialData = filteredTapUUIDs.size() != m.taps().size();
            }

            monitors.add(MonitorDetailsResponse.create(
                    m.uuid(),
                    m.organizationId(),
                    m.tenantId(),
                    m.enabled(),
                    m.type(),
                    m.name(),
                    m.description(),
                    filteredTapUUIDs,
                    m.triggerCondition(),
                    m.interval(),
                    m.filters(),
                    m.alerted(),
                    m.lastEvent(),
                    m.createdAt(),
                    m.updatedAt(),
                    partialData
            ));
        }

        return Response.ok(MonitorListResponse.create(total, monitors)).build();
    }

    @POST
    @Path("/type/{monitor_type}")
    public Response create(@Context SecurityContext sc,
                           @Valid CreateMonitorRequest req,
                           @PathParam("monitor_type") MonitorType monitorType) {
        AuthenticatedUser user = getAuthenticatedUser(sc);

        List<UUID> tapUuids;
        if (req.taps() == null) {
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
        if (!userHasWritePermissionsForMonitorType(user, req.organizationId(), monitorType)) {
            return Response.status(Response.Status.FORBIDDEN).build();
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

    @PUT
    @Path("/show/{id}")
    public Response update(@Context SecurityContext sc,
                           @PathParam("id") UUID uuid,
                           UpdateMonitorRequest req) {
        AuthenticatedUser user = getAuthenticatedUser(sc);
        Optional<MonitorEntry> monitor = nzyme.getMonitors().find(uuid);

        if (monitor.isEmpty() || !entityAccessible(user, monitor.get())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check permissions.
        if (!userHasWritePermissionsForMonitorType(
                user, monitor.get().organizationId(), MonitorType.valueOf(monitor.get().type()))) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        if (req.name() != null && !req.name().isBlank() && req.description() != null && !req.description().isBlank()
                && req.triggerCondition() != null && req.interval() != null)
            nzyme.getMonitors().updateMonitorMetaInformation(
                    monitor.get().uuid(), req.name(), req.description(), req.triggerCondition(), req.interval()
        );

        if (req.filters() != null && !req.filters().isBlank()) {
            List<UUID> tapUuids;
            if (req.taps() == null) {
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

            nzyme.getMonitors().updateMonitorFilterInformation(
                    monitor.get().uuid(), tapUuids, parseFiltersQueryParameter(req.filters())
            );
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("/show/{id}")
    public Response delete(@Context SecurityContext sc, @PathParam("id") UUID uuid) {
        AuthenticatedUser user = getAuthenticatedUser(sc);
        Optional<MonitorEntry> monitor = nzyme.getMonitors().find(uuid);

        if (monitor.isEmpty() || !entityAccessible(user, monitor.get())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check permissions.
        if (!userHasWritePermissionsForMonitorType(
                user, monitor.get().organizationId(), MonitorType.valueOf(monitor.get().type()))) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        nzyme.getMonitors().deleteMonitor(uuid);

        return Response.ok().build();
    }

    private boolean userHasWritePermissionsForMonitorType(AuthenticatedUser user, UUID organizationId, MonitorType monitorType) {
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
                    return false;
            }

            if (!userPermissions.contains(requiredPermission)) {
                return false;
            }
        }

        return true;
    }

}
