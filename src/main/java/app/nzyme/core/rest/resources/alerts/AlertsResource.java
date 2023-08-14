package app.nzyme.core.rest.resources.alerts;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.detection.alerts.DetectionAlertService;
import app.nzyme.core.detection.alerts.db.DetectionAlertAttributeEntry;
import app.nzyme.core.detection.alerts.db.DetectionAlertEntry;
import app.nzyme.core.detection.alerts.db.DetectionAlertTimelineEntry;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.alerts.DetectionAlertDetailsResponse;
import app.nzyme.core.rest.responses.alerts.DetectionAlertListResponse;
import app.nzyme.core.rest.responses.alerts.DetectionAlertTimelineDetailsResponse;
import app.nzyme.core.rest.responses.alerts.DetectionAlertTimelineListResponse;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Path("/api/alerts")
@Produces(MediaType.APPLICATION_JSON)
public class AlertsResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(UserAuthenticatedResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "alerts_view" })
    public Response findAll(@Context SecurityContext sc,
                            @QueryParam("limit") int limit,
                            @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<DetectionAlertEntry> alerts = nzyme.getDetectionAlertService().findAllAlerts(
                authenticatedUser.getOrganizationId(),
                authenticatedUser.getTenantId(),
                limit,
                offset
        );

        long total = nzyme.getDetectionAlertService().countAlerts(
                authenticatedUser.getOrganizationId(),
                authenticatedUser.getTenantId()
        );

        List<DetectionAlertDetailsResponse> responsesList = Lists.newArrayList();
        for (DetectionAlertEntry alert : alerts) {
            List<DetectionAlertAttributeEntry> attributes = nzyme.getDetectionAlertService()
                    .findAlertAttributes(alert.id());

            responsesList.add(buildDetailsResponse(alert, attributes));
        }

        return Response.ok(DetectionAlertListResponse.create(total, responsesList)).build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "alerts_view" })
    @Path("/show/{uuid}")
    public Response findOne(@Context SecurityContext sc, @PathParam("uuid") UUID uuid) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<DetectionAlertEntry> alert = nzyme.getDetectionAlertService().findAlert(uuid,
                authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (alert.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<DetectionAlertAttributeEntry> attributes = nzyme.getDetectionAlertService()
                .findAlertAttributes(alert.get().id());

        return Response.ok(buildDetailsResponse(alert.get(), attributes)).build();
    }


    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "alerts_view" })
    @Path("/show/{uuid}/timeline")
    public Response findTimeline(@Context SecurityContext sc,
                                 @PathParam("uuid") UUID uuid,
                                 @QueryParam("limit") int limit,
                                 @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<DetectionAlertEntry> alert = nzyme.getDetectionAlertService().findAlert(uuid,
                authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (alert.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<DetectionAlertTimelineDetailsResponse> entries = Lists.newArrayList();
        for (DetectionAlertTimelineEntry timelineEntry : nzyme.getDetectionAlertService()
                .findAlertTimeline(alert.get().id(), limit, offset)) {
            Duration duration = new Duration(timelineEntry.seenFrom(), timelineEntry.seenTo());

            entries.add(DetectionAlertTimelineDetailsResponse.create(
                    timelineEntry.seenFrom(),
                    timelineEntry.seenTo(),
                    duration.getStandardSeconds(),
                    Tools.durationToHumanReadable(duration)
            ));
        }

        long total = nzyme.getDetectionAlertService().countAlertTimelineEntries(alert.get().id());

        return Response.ok(DetectionAlertTimelineListResponse.create(total, entries)).build();
    }

    private DetectionAlertDetailsResponse buildDetailsResponse(DetectionAlertEntry alert,
                                                               List<DetectionAlertAttributeEntry> attributes) {
        Map<String, String> responseAttributes = Maps.newTreeMap();
        for (DetectionAlertAttributeEntry attribute : attributes) {
            responseAttributes.put(attribute.key(), attribute.value());
        }

        return DetectionAlertDetailsResponse.create(
                alert.uuid(),
                alert.dot11MonitoredNetworkId(),
                alert.tapId(),
                alert.detectionType(),
                alert.subsystem(),
                alert.details(),
                responseAttributes,
                alert.createdAt(),
                alert.lastSeen(),
                alert.lastSeen().isAfter(DateTime.now().minusMinutes(DetectionAlertService.ACTIVE_THRESHOLD_MINUTES)),
                alert.organizationId(),
                alert.tenantId()
        );
    }

}
