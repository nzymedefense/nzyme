package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.monitoring.probereq.MonitoredProbeRequestEntry;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.requests.CreateMonitoredProbeRequestRequest;
import app.nzyme.core.rest.requests.UpdateMonitoredProbeRequestRequest;
import app.nzyme.core.rest.responses.dot11.monitoring.probereq.MonitoredProbeRequestDetailsResponse;
import app.nzyme.core.rest.responses.dot11.monitoring.probereq.MonitoredProbeRequestListResponse;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/dot11/monitoring/proberequests")
@Produces(MediaType.APPLICATION_JSON)
public class Dot11MonitoredProbeRequestResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(Dot11MonitoredProbeRequestResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    public Response findAll(@Context SecurityContext sc,
                            @QueryParam("limit") int limit,
                            @QueryParam("offset") int offset,
                            @QueryParam("organization_uuid") @NotNull UUID organizationId,
                            @QueryParam("tenant_uuid") @NotNull UUID tenantId) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long total = nzyme.getDot11().countAllMonitoredProbeRequests(organizationId, tenantId);

        List<MonitoredProbeRequestDetailsResponse> ssids = Lists.newArrayList();
        for (MonitoredProbeRequestEntry ssid : nzyme.getDot11()
                .findAllMonitoredProbeRequests(organizationId, tenantId, limit, offset)) {
            ssids.add(MonitoredProbeRequestDetailsResponse.create(
                    ssid.uuid(),
                    ssid.organizationId(),
                    ssid.tenantId(),
                    ssid.ssid(),
                    ssid.notes(),
                    ssid.updatedAt(),
                    ssid.createdAt()
            ));
        }

        return Response.ok(MonitoredProbeRequestListResponse.create(total, ssids)).build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/show/{uuid}")
    public Response findOne(@Context SecurityContext sc,
                            @PathParam("uuid") UUID uuid,
                            @QueryParam("organization_uuid") @NotNull UUID organizationId,
                            @QueryParam("tenant_uuid") @NotNull UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<MonitoredProbeRequestEntry> ssid = nzyme.getDot11()
                .findMonitoredProbeRequest(uuid, organizationId, tenantId);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        MonitoredProbeRequestDetailsResponse response = MonitoredProbeRequestDetailsResponse.create(
                ssid.get().uuid(),
                ssid.get().organizationId(),
                ssid.get().tenantId(),
                ssid.get().ssid(),
                ssid.get().notes(),
                ssid.get().updatedAt(),
                ssid.get().createdAt()
        );

        return Response.ok(response).build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    public Response create(@Context SecurityContext sc,
                           @Valid CreateMonitoredProbeRequestRequest req) {
        if (!passedTenantDataAccessible(sc, req.organizationId(), req.tenantId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().createMonitoredProbeRequest(req.organizationId(), req.tenantId(), req.ssid(), req.notes());

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/show/{uuid}")
    public Response update(@Context SecurityContext sc,
                           @Valid UpdateMonitoredProbeRequestRequest req,
                           @PathParam("uuid") UUID uuid) {
        if (!passedTenantDataAccessible(sc, req.organizationId(), req.tenantId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<MonitoredProbeRequestEntry> ssid = nzyme.getDot11()
                .findMonitoredProbeRequest(uuid, req.organizationId(), req.tenantId());

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().updateMonitoredProbeRequest(uuid, req.organizationId(), req.tenantId(), req.ssid(), req.notes());

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/show/{uuid}")
    public Response delete(@Context SecurityContext sc, @PathParam("uuid") UUID uuid) {
        Optional<MonitoredProbeRequestEntry> ssid = nzyme.getDot11()
                .findMonitoredProbeRequest(uuid);

        if (ssid.isEmpty() || !passedTenantDataAccessible(sc, ssid.get().organizationId(), ssid.get().tenantId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteMonitoredProbeRequest(uuid, ssid.get().organizationId(), ssid.get().tenantId());

        return Response.ok().build();
    }

}
