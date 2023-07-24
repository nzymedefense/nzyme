package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.monitoring.MonitoredSSID;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.CreateDot11MonitoredNetworkRequest;
import app.nzyme.core.rest.resources.system.authentication.AuthenticationResource;
import app.nzyme.core.rest.responses.dot11.monitoring.MonitoredSSIDDetailsResponse;
import app.nzyme.core.rest.responses.dot11.monitoring.MonitoredSSIDListResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/api/dot11/monitoring")
@Produces(MediaType.APPLICATION_JSON)
public class Dot11MonitoredNetworksResource extends AuthenticationResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids")
    public Response findAll(@Context SecurityContext sc, @Valid CreateDot11MonitoredNetworkRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        List<MonitoredSSIDDetailsResponse> ssids = Lists.newArrayList();
        for (MonitoredSSID ssid : nzyme.getDot11().findAllMonitoredSSIDs(
                authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId())) {
            boolean isAlerted = false;
            ssids.add(MonitoredSSIDDetailsResponse.create(
                    ssid.uuid(),
                    ssid.ssid(),
                    ssid.organizationId(),
                    ssid.tenantId(),
                    ssid.createdAt(),
                    ssid.updatedAt(),
                    isAlerted
            ));
        }

        return Response.ok(MonitoredSSIDListResponse.create(ssids)).build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids")
    public Response createMonitoredSSID(@Context SecurityContext sc, @Valid CreateDot11MonitoredNetworkRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        nzyme.getDot11().createMonitoredSSID(
                req.ssid(),
                authenticatedUser.getOrganizationId(),
                authenticatedUser.getTenantId()
        );

        return Response.status(Response.Status.CREATED).build();
    }

}
