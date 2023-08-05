package app.nzyme.core.rest.resources.alerts;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.detection.alerts.db.DetectionAlertAttributeEntry;
import app.nzyme.core.detection.alerts.db.DetectionAlertEntry;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.alerts.DetectionAlertDetailsResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Map;

@Path("/api/alerts")
@Produces(MediaType.APPLICATION_JSON)
public class AlertsResource extends UserAuthenticatedResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "alerts_view" })
    public Response findAll(@Context SecurityContext sc) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        List<DetectionAlertEntry> alerts = nzyme.getDetectionAlertService().findAllAlerts(
                authenticatedUser.getOrganizationId(),
                authenticatedUser.getTenantId(),
                25,
                0
        );

        List<DetectionAlertDetailsResponse> response = Lists.newArrayList();
        for (DetectionAlertEntry alert : alerts) {
            List<DetectionAlertAttributeEntry> attributes = nzyme.getDetectionAlertService()
                    .findAlertAttributes(alert.id());

            Map<String, String> responseAttributes = Maps.newTreeMap();
            for (DetectionAlertAttributeEntry attribute : attributes) {
                responseAttributes.put(attribute.key(), attribute.value());
            }

            response.add(DetectionAlertDetailsResponse.create(
                    alert.uuid(),
                    alert.dot11MonitoredNetworkId(),
                    alert.tapId(),
                    alert.detectionType(),
                    alert.subsystem(),
                    responseAttributes,
                    alert.createdAt(),
                    alert.lastSeen(),
                    alert.organizationId(),
                    alert.tenantId()
            ));
        }

        return Response.ok(response).build();
    }

}
