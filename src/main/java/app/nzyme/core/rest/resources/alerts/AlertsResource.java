package app.nzyme.core.rest.resources.alerts;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.detection.alerts.DetectionAlertService;
import app.nzyme.core.detection.alerts.DetectionType;
import app.nzyme.core.detection.alerts.Subsystem;
import app.nzyme.core.detection.alerts.db.DetectionAlertAttributeEntry;
import app.nzyme.core.detection.alerts.db.DetectionAlertEntry;
import app.nzyme.core.detection.alerts.db.DetectionAlertTimelineEntry;
import app.nzyme.core.events.EventEngine;
import app.nzyme.core.events.EventEngineImpl;
import app.nzyme.core.events.db.EventActionEntry;
import app.nzyme.core.events.db.SubscriptionEntry;
import app.nzyme.core.events.types.EventActionType;
import app.nzyme.core.events.types.EventType;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.DetectionEventSubscriptionRequest;
import app.nzyme.core.rest.requests.SystemEventSubscriptionRequest;
import app.nzyme.core.rest.requests.UUIDListRequest;
import app.nzyme.core.rest.responses.alerts.*;
import app.nzyme.core.rest.responses.events.SubscriptionDetailsResponse;
import app.nzyme.core.rest.responses.misc.ErrorResponse;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.annotation.Nullable;
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

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "alerts_manage" })
    @Path("/show/{uuid}")
    public Response delete(@Context SecurityContext sc, @PathParam("uuid") UUID uuid) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<DetectionAlertEntry> alert = nzyme.getDetectionAlertService().findAlert(uuid,
                authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (alert.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDetectionAlertService().delete(uuid);

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "alerts_manage" })
    @Path("/show/{uuid}/resolve")
    public Response markAsResolved(@Context SecurityContext sc, @PathParam("uuid") UUID uuid) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<DetectionAlertEntry> alert = nzyme.getDetectionAlertService().findAlert(uuid,
                authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (alert.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDetectionAlertService().markAlertAsResolved(uuid);

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "alerts_manage" })
    @Path("/many/resolve")
    public Response markListAsResolved(@Context SecurityContext sc, UUIDListRequest uuids) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        for (UUID uuid : uuids.uuids()) {
            Optional<DetectionAlertEntry> alert = nzyme.getDetectionAlertService().findAlert(uuid,
                    authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

            if (alert.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            nzyme.getDetectionAlertService().markAlertAsResolved(uuid);
        }

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "alerts_manage" })
    @Path("/many/delete")
    public Response deleteList(@Context SecurityContext sc, UUIDListRequest uuids) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        for (UUID uuid : uuids.uuids()) {
            Optional<DetectionAlertEntry> alert = nzyme.getDetectionAlertService().findAlert(uuid,
                    authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

            if (alert.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            nzyme.getDetectionAlertService().delete(uuid);
        }

        return Response.ok().build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ORGADMINISTRATOR)
    @Path("/detections/types")
    public Response findAllDetectionTypes(@Context SecurityContext sc,
                                          @QueryParam("limit") int limit,
                                          @QueryParam("offset") int offset,
                                          @QueryParam("organization_uuid") @NotNull UUID filterOrganizationId,
                                          @QueryParam("filter_subsystem") @Nullable String filterSubsystem) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        UUID organizationId;
        if (authenticatedUser.isSuperAdministrator()) {
            if (filterOrganizationId != null) {
                organizationId = filterOrganizationId;
            } else {
                // Super admins have to select an org.
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            organizationId = authenticatedUser.getOrganizationId();
        }

        // Filters.
        Subsystem subsystem = null;
        if (!Strings.isNullOrEmpty(filterSubsystem)) {
            try {
                subsystem = Subsystem.valueOf(filterSubsystem.toUpperCase());
            } catch(IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        // Find all detection types.
        List<DetectionType> types = nzyme.getDetectionAlertService().findAllDetectionTypes(subsystem, limit, offset);
        long totalCount = nzyme.getDetectionAlertService().countAllDetectionTypes(subsystem);

        EventEngineImpl eventEngine = (EventEngineImpl) nzyme.getEventEngine();
        List<DetectionAlertTypeDetailsResponse> typesList = Lists.newArrayList();
        for (DetectionType type : types) {
            List<SubscriptionDetailsResponse> subscriptions = Lists.newArrayList();
            for (SubscriptionEntry sub : eventEngine.findAllActionsOfSubscription(organizationId, type.name())) {
                eventEngine.findEventAction(sub.actionId()).ifPresent(eventActionEntry ->
                        subscriptions.add(buildSubscriptionDetailsResponse(sub, eventActionEntry))
                );
            }

            typesList.add(DetectionAlertTypeDetailsResponse.create(
                    type.name(),
                    type.getTitle(),
                    type.getSubsystem().name(),
                    subscriptions
            ));
        }

        // Build response.
        return Response.ok(DetectionAlertTypeListResponse.create(totalCount, typesList)).build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ORGADMINISTRATOR)
    @Path("/detections/subscriptions/wildcard")
    public Response findAllWildcardSubscription(@Context SecurityContext sc,
                                                @QueryParam("organization_uuid") @NotNull UUID filterOrganizationId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        UUID organizationId;
        if (authenticatedUser.isSuperAdministrator()) {
            if (filterOrganizationId != null) {
                organizationId = filterOrganizationId;
            } else {
                // Super admins have to select an org.
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            organizationId = authenticatedUser.getOrganizationId();
        }

        EventEngineImpl eventEngine = (EventEngineImpl) nzyme.getEventEngine();
        List<SubscriptionDetailsResponse> subscriptions = Lists.newArrayList();
        for (SubscriptionEntry sub : eventEngine.findAllActionsOfSubscription(organizationId, "*")) {
            eventEngine.findEventAction(sub.actionId()).ifPresent(eventActionEntry ->
                    subscriptions.add(buildSubscriptionDetailsResponse(sub, eventActionEntry))
            );
        }

        return Response.ok(subscriptions).build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ORGADMINISTRATOR)
    @Path("/detections/subscriptions/wildcard")
    public Response subscribeWildcardAction(@Context SecurityContext sc,
                                            @Valid DetectionEventSubscriptionRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!authenticatedUser.isSuperAdministrator()
                && !req.organizationId().equals(authenticatedUser.getOrganizationId())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        EventEngineImpl eventEngine = ((EventEngineImpl) nzyme.getEventEngine());

        // Pull action.
        Optional<EventActionEntry> action = eventEngine.findEventAction(req.actionId());

        if (action.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if this event already has this action ID subscribed to it.
        for (SubscriptionEntry sub : eventEngine
                .findAllActionsOfSubscription(req.organizationId(), "*")) {
            if (sub.actionId().equals(action.get().uuid())) {
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.create("Action is already subscribed."))
                        .build();
            }
        }

        eventEngine.subscribeActionToEvent(req.organizationId(), EventType.DETECTION, "*", action.get().uuid());

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ORGADMINISTRATOR)
    @Path("/detections/subscriptions/wildcard/show/{subscriptionId}")
    public Response unsubscribeWildcardAction(@Context SecurityContext sc,
                                              @PathParam("subscriptionId") @NotNull UUID subscriptionId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        EventEngineImpl eventEngine = ((EventEngineImpl) nzyme.getEventEngine());

        // Fetch action UUID from subscription.
        Optional<UUID> actionUuid = eventEngine.findActionOfSubscription(subscriptionId);

        if (actionUuid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Fetch action.
        Optional<EventActionEntry> action = eventEngine.findEventAction(actionUuid.get());

        if (action.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if superadmin or event is subscription of org.
        if (!authenticatedUser.isSuperAdministrator()) {
            if (action.get().organizationId() == null
                    || !(action.get().organizationId().equals(authenticatedUser.getOrganizationId()))) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }

        // Delete subscription.
        eventEngine.unsubscribeActionFromEvent(subscriptionId);

        return Response.ok().build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ORGADMINISTRATOR)
    @Path("/detections/types/show/{name}")
    public Response findDetectionType(@Context SecurityContext sc,
                                      @QueryParam("organization_uuid") @NotNull UUID filterOrganizationId,
                                      @PathParam("name") @NotEmpty String name) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        UUID organizationId;
        if (authenticatedUser.isSuperAdministrator()) {
            if (filterOrganizationId != null) {
                organizationId = filterOrganizationId;
            } else {
                // Super admins have to select an org.
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            organizationId = authenticatedUser.getOrganizationId();
        }

        DetectionType requestedType = null;
        for (DetectionType type : nzyme.getDetectionAlertService().findAllDetectionTypes(null, Integer.MAX_VALUE, 0)) {
            if (type.name().equals(name.toUpperCase())) {
                requestedType = type;
            }
        }

        if (requestedType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        EventEngineImpl eventEngine = (EventEngineImpl) nzyme.getEventEngine();
        List<SubscriptionDetailsResponse> subscriptions = Lists.newArrayList();
        for (SubscriptionEntry sub : eventEngine.findAllActionsOfSubscription(organizationId, requestedType.name())) {
            eventEngine.findEventAction(sub.actionId()).ifPresent(eventActionEntry ->
                    subscriptions.add(buildSubscriptionDetailsResponse(sub, eventActionEntry))
            );
        }

        return Response.ok(
                DetectionAlertTypeDetailsResponse.create(
                    requestedType.name(),
                    requestedType.getTitle(),
                    requestedType.getSubsystem().name(),
                    subscriptions
                )
        ).build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ORGADMINISTRATOR)
    @Path("/detections/types/show/{detectionTypeName}/subscriptions")
    public Response subscribeActionToDetectionEvent(@Context SecurityContext sc,
                                                    @PathParam("detectionTypeName") @NotEmpty String detectionTypeName,
                                                    @Valid DetectionEventSubscriptionRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!authenticatedUser.isSuperAdministrator()
                && !req.organizationId().equals(authenticatedUser.getOrganizationId())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        DetectionType detectionType;
        try {
            detectionType = DetectionType.valueOf(detectionTypeName.toUpperCase());
        } catch(IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        EventEngineImpl eventEngine = ((EventEngineImpl) nzyme.getEventEngine());

        // Pull action.
        Optional<EventActionEntry> action = eventEngine.findEventAction(req.actionId());

        if (action.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if this event already has this action ID subscribed to it.
        for (SubscriptionEntry sub : eventEngine
                .findAllActionsOfSubscription(req.organizationId(), detectionType.name())) {
            if (sub.actionId().equals(action.get().uuid())) {
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.create("Action is already subscribed to this event."))
                        .build();
            }
        }

        eventEngine.subscribeActionToEvent(
                req.organizationId(), EventType.DETECTION, detectionType.name(), action.get().uuid());

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ORGADMINISTRATOR)
    @Path("/detections/types/show/{detectionTypeName}/subscriptions/show/{subscriptionId}")
    public Response unsubscribeActionFromDetectionEvent(@Context SecurityContext sc,
                                                        @PathParam("subscriptionId") @NotNull UUID subscriptionId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        EventEngineImpl eventEngine = ((EventEngineImpl) nzyme.getEventEngine());

        // Fetch action UUID from subscription.
        Optional<UUID> actionUuid = eventEngine.findActionOfSubscription(subscriptionId);

        if (actionUuid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Fetch action.
        Optional<EventActionEntry> action = eventEngine.findEventAction(actionUuid.get());

        if (action.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if superadmin or event is subscription of org.
        if (!authenticatedUser.isSuperAdministrator()) {
            if (action.get().organizationId() == null
                    || !(action.get().organizationId().equals(authenticatedUser.getOrganizationId()))) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
       }

        // Delete subscription.
        eventEngine.unsubscribeActionFromEvent(subscriptionId);

        return Response.ok().build();
    }

    private DetectionAlertDetailsResponse buildDetailsResponse(DetectionAlertEntry alert,
                                                               List<DetectionAlertAttributeEntry> attributes) {
        Map<String, String> responseAttributes = Maps.newTreeMap();
        for (DetectionAlertAttributeEntry attribute : attributes) {
            responseAttributes.put(attribute.key(), attribute.value());
        }

        boolean isActive = !alert.isResolved() &&
                alert.lastSeen().isAfter(DateTime.now().minusMinutes(DetectionAlertService.ACTIVE_THRESHOLD_MINUTES));

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
                isActive,
                alert.organizationId(),
                alert.tenantId()
        );
    }

    private static SubscriptionDetailsResponse buildSubscriptionDetailsResponse(SubscriptionEntry subscriptionEntry,
                                                                                EventActionEntry eventActionEntry) {
        EventActionType eventActionType = EventActionType.valueOf(eventActionEntry.actionType());

        return SubscriptionDetailsResponse.create(
                subscriptionEntry.uuid(),
                eventActionEntry.uuid(),
                eventActionType.name(),
                eventActionType.getHumanReadable(),
                eventActionEntry.name()
        );
    }

}
