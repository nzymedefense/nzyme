package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.Dot11KnownNetwork;
import app.nzyme.core.dot11.monitoring.ssids.MonitoredSSIDRegistryKeys;
import app.nzyme.core.integrations.smtp.SMTPConfigurationRegistryKeys;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.requests.UpdateConfigurationRequest;
import app.nzyme.core.rest.responses.dot11.monitoring.ssids.KnownNetworkDetailsResponse;
import app.nzyme.core.rest.responses.dot11.monitoring.ssids.KnownNetworksListResponse;
import app.nzyme.core.rest.responses.dot11.monitoring.ssids.SSIDMonitoringConfigurationResponse;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraintValidator;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryValueType;
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

import java.util.*;

@Path("/api/dot11/monitoring/networks")
@Produces(MediaType.APPLICATION_JSON)
public class Dot11MonitoredSSIDsResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(Dot11MonitoredSSIDsResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/organization/{organization_id}/tenant/{tenant_id}")
    public Response findAll(@Context SecurityContext sc,
                            @QueryParam("limit") int limit,
                            @QueryParam("offset") int offset,
                            @PathParam("organization_id") @NotNull UUID organizationId,
                            @PathParam("tenant_id") @NotNull UUID tenantId) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long total = nzyme.getDot11().countAllKnownNetworks(organizationId, tenantId);
        List<KnownNetworkDetailsResponse> networks = Lists.newArrayList();
        for (Dot11KnownNetwork kn : nzyme.getDot11().findAllKnownNetworks(organizationId, tenantId, limit, offset)) {
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
    @Path("/organization/{organization_id}/tenant/{tenant_id}/show/{uuid}/approve")
    public Response approve(@Context SecurityContext sc,
                            @PathParam("uuid") UUID uuid,
                            @PathParam("organization_id") @NotNull UUID organizationId,
                            @PathParam("tenant_id") @NotNull UUID tenantId) {
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
    @Path("/organization/{organization_id}/tenant/{tenant_id}/show/{uuid}/revoke")
    public Response revoke(@Context SecurityContext sc,
                           @PathParam("uuid") UUID uuid,
                           @PathParam("organization_id") @NotNull UUID organizationId,
                           @PathParam("tenant_id") @NotNull UUID tenantId) {
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
    @Path("/organization/{organization_id}/tenant/{tenant_id}/show/{uuid}")
    public Response deleteSingle(@Context SecurityContext sc,
                                 @PathParam("uuid") UUID uuid,
                                 @PathParam("organization_id") @NotNull UUID organizationId,
                                 @PathParam("tenant_id") @NotNull UUID tenantId) {
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
    @Path("/organization/{organization_id}/tenant/{tenant_id}")
    public Response deleteAllOfTenant(@Context SecurityContext sc,
                                      @PathParam("organization_id") @NotNull UUID organizationId,
                                      @PathParam("tenant_id") @NotNull UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteKnownNetworksOfTenant(organizationId, tenantId);

        return Response.ok().build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/organization/{organization_id}/tenant/{tenant_id}/configuration")
    public Response configuration(@Context SecurityContext sc,
                                  @PathParam("organization_id") @NotNull UUID organizationId,
                                  @PathParam("tenant_id") @NotNull UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<String> isEnabled = nzyme.getDatabaseCoreRegistry().getValue(
                MonitoredSSIDRegistryKeys.IS_ENABLED.key(), organizationId, tenantId
        );

        Optional<String> eventingIsEnabled = nzyme.getDatabaseCoreRegistry().getValue(
                MonitoredSSIDRegistryKeys.EVENTING_IS_ENABLED.key(), organizationId, tenantId
        );

        SSIDMonitoringConfigurationResponse configuration = SSIDMonitoringConfigurationResponse.create(
                ConfigurationEntryResponse.create(
                        MonitoredSSIDRegistryKeys.IS_ENABLED.key(),
                        "Is enabled",
                        isEnabled.isPresent() && isEnabled.get().equals("true"),
                        ConfigurationEntryValueType.BOOLEAN,
                        MonitoredSSIDRegistryKeys.IS_ENABLED.defaultValue().orElse(null),
                        MonitoredSSIDRegistryKeys.IS_ENABLED.requiresRestart(),
                        MonitoredSSIDRegistryKeys.IS_ENABLED.constraints().orElse(Collections.emptyList()),
                        "ssid-monitoring"
                ),
                ConfigurationEntryResponse.create(
                        MonitoredSSIDRegistryKeys.EVENTING_IS_ENABLED.key(),
                        "Event generation is enabled",
                        eventingIsEnabled.isPresent() && eventingIsEnabled.get().equals("true"),
                        ConfigurationEntryValueType.BOOLEAN,
                        MonitoredSSIDRegistryKeys.EVENTING_IS_ENABLED.defaultValue().orElse(null),
                        MonitoredSSIDRegistryKeys.EVENTING_IS_ENABLED.requiresRestart(),
                        MonitoredSSIDRegistryKeys.EVENTING_IS_ENABLED.constraints().orElse(Collections.emptyList()),
                        "ssid-monitoring"
                )
        );

        return Response.ok(configuration).build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/organization/{organization_id}/tenant/{tenant_id}/configuration")
    public Response updateConfiguration(@Context SecurityContext sc,
                                        UpdateConfigurationRequest req,
                                        @PathParam("organization_id") @NotNull UUID organizationId,
                                        @PathParam("tenant_id") @NotNull UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (req.change().isEmpty()) {
            LOG.info("Empty configuration parameters.");
            return Response.status(422).build();
        }

        for (Map.Entry<String, Object> c : req.change().entrySet()) {
            switch (c.getKey()) {
                case "is_enabled":
                    if (!ConfigurationEntryConstraintValidator.checkConstraints(MonitoredSSIDRegistryKeys.IS_ENABLED, c)) {
                        return Response.status(422).build();
                    }
                    break;
                case "eventing_is_enabled":
                    if (!ConfigurationEntryConstraintValidator.checkConstraints(MonitoredSSIDRegistryKeys.EVENTING_IS_ENABLED, c)) {
                        return Response.status(422).build();
                    }
                    break;
            }

            nzyme.getDatabaseCoreRegistry().setValue(c.getKey(), c.getValue().toString(), organizationId, tenantId);
        }

        return Response.ok().build();
    }

}
