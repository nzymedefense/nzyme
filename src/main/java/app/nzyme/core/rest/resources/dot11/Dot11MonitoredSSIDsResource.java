package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.Dot11KnownNetwork;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.responses.dot11.monitoring.ssids.KnownNetworkDetailsResponse;
import app.nzyme.core.rest.responses.dot11.monitoring.ssids.KnownNetworksListResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
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

@Path("/api/dot11/monitoring/ssids")
@Produces(MediaType.APPLICATION_JSON)
public class Dot11MonitoredSSIDsResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(Dot11MonitoredSSIDsResource.class);

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

        long total = nzyme.getDot11().countAllKnownNetworks(organizationId, tenantId);
        List<KnownNetworkDetailsResponse> networks = Lists.newArrayList();
        for (Dot11KnownNetwork kn : nzyme.getDot11().findAllKnownNetworks(organizationId, tenantId)) {
            networks.add(KnownNetworkDetailsResponse.create(
                    kn.uuid(),
                    kn.organizationId(),
                    kn.tenantId(),
                    kn.ssid(),
                    kn.isApproved(),
                    kn.firstSeen(),
                    kn.lastSeen()
            ));
        }

        return Response.ok(KnownNetworksListResponse.create(total, networks)).build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/show/{uuid}/approve")
    public Response approve(@Context SecurityContext sc,
                            @PathParam("uuid") UUID uuid,
                            @QueryParam("organization_uuid") @NotNull UUID organizationId,
                            @QueryParam("tenant_uuid") @NotNull UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Dot11KnownNetwork> knownNetwork = nzyme.getDot11().findKnownNetwork(uuid, organizationId, tenantId);
        if (knownNetwork.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().changeStatusOfKnownNetwork(knownNetwork.get().id(), true);

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/show/{uuid}/revoke")
    public Response revoke(@Context SecurityContext sc,
                           @PathParam("uuid") UUID uuid,
                           @QueryParam("organization_uuid") @NotNull UUID organizationId,
                           @QueryParam("tenant_uuid") @NotNull UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Dot11KnownNetwork> knownNetwork = nzyme.getDot11().findKnownNetwork(uuid, organizationId, tenantId);
        if (knownNetwork.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().changeStatusOfKnownNetwork(knownNetwork.get().id(), false);

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/show/{uuid}")
    public Response deleteSingle(@Context SecurityContext sc,
                                 @PathParam("uuid") UUID uuid,
                                 @QueryParam("organization_uuid") @NotNull UUID organizationId,
                                 @QueryParam("tenant_uuid") @NotNull UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Dot11KnownNetwork> knownNetwork = nzyme.getDot11().findKnownNetwork(uuid, organizationId, tenantId);
        if (knownNetwork.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteKnownNetwork(knownNetwork.get().id());

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/reset")
    public Response deleteAllOfTenant(@Context SecurityContext sc,
                                     @QueryParam("organization_uuid") @NotNull UUID organizationId,
                                     @QueryParam("tenant_uuid") @NotNull UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteKnownNetworksOfTenant(organizationId, tenantId);

        return Response.ok().build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/configuration")
    public Response configuration(@Context SecurityContext sc,
                                  @QueryParam("organization_uuid") @NotNull UUID organizationId,
                                  @QueryParam("tenant_uuid") @NotNull UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // TODO return configuration (enabled/disabled, required device activity time (5))

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/enable")
    public Response enable(@Context SecurityContext sc,
                           @QueryParam("organization_uuid") @NotNull UUID organizationId,
                           @QueryParam("tenant_uuid") @NotNull UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // TODO enable setting

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/disable")
    public Response disable(@Context SecurityContext sc,
                            @QueryParam("organization_uuid") @NotNull UUID organizationId,
                            @QueryParam("tenant_uuid") @NotNull UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // TODO disable setting

        return Response.ok().build();
    }

}
