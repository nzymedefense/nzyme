package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.events.EventEngineImpl;
import app.nzyme.core.events.db.EventActionEntry;
import app.nzyme.core.events.db.EventEntry;
import app.nzyme.core.events.db.SubscriptionEntry;
import app.nzyme.core.events.types.EventActionType;
import app.nzyme.core.events.types.EventType;
import app.nzyme.core.events.types.SystemEventScope;
import app.nzyme.core.events.types.SystemEventType;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.SystemEventSubscriptionRequest;
import app.nzyme.core.rest.responses.events.*;
import app.nzyme.core.rest.responses.misc.ErrorResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/system/events")
@Produces(MediaType.APPLICATION_JSON)
public class EventsResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(UserAuthenticatedResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    public Response findAllEvents(@Context SecurityContext sc,
                                  @QueryParam("limit") int limit,
                                  @QueryParam("offset") int offset,
                                  @QueryParam("event_types") String eventTypes) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (Strings.isNullOrEmpty(eventTypes)) {
            return Response.ok(EventsListResponse.create(0, Collections.emptyList())).build();
        }

        List<String> types = Splitter.on(",").splitToList(eventTypes);

        List<EventEntry> events;
        long totalEvents;
        if (authenticatedUser.isSuperAdministrator()) {
            events = ((EventEngineImpl) nzyme.getEventEngine()).findAllEventsOfAllOrganizations(types, limit, offset);
            totalEvents = ((EventEngineImpl) nzyme.getEventEngine()).countAllEventsOfAllOrganizations();
        } else {
            // Organization admin.
            events = ((EventEngineImpl) nzyme.getEventEngine())
                    .findAllEventsOfOrganization(types, authenticatedUser.getOrganizationId(), limit, offset);
            totalEvents = ((EventEngineImpl) nzyme.getEventEngine()).countAllEventsOfOrganization(authenticatedUser.getOrganizationId());
        }

        List<EventDetailsResponse> result = Lists.newArrayList();
        for (EventEntry event : events) {
            result.add(EventDetailsResponse.create(
                    event.uuid(),
                    event.eventType(),
                    event.reference(),
                    event.details(),
                    event.createdAt()
            ));
        }

        return Response.ok(EventsListResponse.create(totalEvents, result)).build();
    }

    @GET
    @Path("/types")
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    public Response findAllEventTypes(@Context SecurityContext sc,
                                      @QueryParam("limit") int limit,
                                      @QueryParam("offset") int offset,
                                      @QueryParam("categories") String eventCategories,
                                      @QueryParam("organization_id") @Nullable UUID organizationId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        // Org admins can only request data for own org. (types are always the same, but subs are not)
        if (!authenticatedUser.isSuperAdministrator()) {
            if (organizationId == null || !organizationId.equals(authenticatedUser.getOrganizationId())) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }

        if (Strings.isNullOrEmpty(eventCategories)) {
            return Response.ok(EventTypesListResponse.create(0, Collections.emptyList())).build();
        }

        List<String> categories = Splitter.on(",").splitToList(eventCategories);

        List<SystemEventType> types = Lists.newArrayList();
        int totalEvents = 0;
        for (SystemEventType type : SystemEventType.values()) {
            if (!authenticatedUser.isSuperAdministrator() && type.getScope().equals(SystemEventScope.SYSTEM)) {
                // Skip system event types for non-superadmins.
                continue;
            }

            if (authenticatedUser.isSuperAdministrator() && type.getScope().equals(SystemEventScope.ORGANIZATION)) {
                // Skip organization event types for superadmins.
                continue;
            }

            if (categories.contains(type.getCategory().toString())) {
                types.add(type);
                totalEvents++;
            }
        }

        List<SystemEventType> page = types.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

        EventEngineImpl eventEngine = ((EventEngineImpl) nzyme.getEventEngine());

        List<EventTypeDetailsResponse> result = Lists.newArrayList();
        for (SystemEventType entry : page) {
            List<SubscriptionDetailsResponse> subscriptions = Lists.newArrayList();
            for (SubscriptionEntry sub : eventEngine.findAllActionsOfSubscription(authenticatedUser.getOrganizationId(), entry.name())) {
                eventEngine.findEventAction(sub.actionId()).ifPresent(eventActionEntry ->
                    subscriptions.add(buildSubscriptionDetailsResponse(sub, eventActionEntry))
                );
            }

            result.add(EventTypeDetailsResponse.create(
                    entry.name(),
                    entry.getCategory().name(),
                    entry.getCategory().getHumanReadableName(),
                    entry.getHumanReadableName(),
                    entry.getDescription(),
                    subscriptions
            ));
        }

        return Response.ok(EventTypesListResponse.create(totalEvents, result)).build();
    }

    @GET
    @Path("/types/system/show/{eventTypeName}")
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    public Response findSystemEventType(@Context SecurityContext sc,
                                        @PathParam("eventTypeName") @NotEmpty String eventTypeName,
                                        @QueryParam("organization_id") @Nullable UUID organizationId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        // Org admins can only request data for own org. (types are always the same, but subs are not)
        if (!authenticatedUser.isSuperAdministrator()) {
            if (organizationId == null || !organizationId.equals(authenticatedUser.getOrganizationId())) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }

        SystemEventType eventType;
        try {
            eventType = SystemEventType.valueOf(eventTypeName.toUpperCase());
        } catch(IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!authenticatedUser.isSuperAdministrator() && !eventType.getScope().equals(SystemEventScope.ORGANIZATION)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        EventEngineImpl eventEngine = ((EventEngineImpl) nzyme.getEventEngine());
        List<SubscriptionDetailsResponse> subscriptions = Lists.newArrayList();
        for (SubscriptionEntry sub : eventEngine.findAllActionsOfSubscription(authenticatedUser.getOrganizationId(), eventType.name())) {
            eventEngine.findEventAction(sub.actionId()).ifPresent(eventActionEntry ->
                    subscriptions.add(buildSubscriptionDetailsResponse(sub, eventActionEntry))
            );
        }

        return Response.ok(EventTypeDetailsResponse.create(
                eventType.name(),
                eventType.getCategory().name(),
                eventType.getCategory().getHumanReadableName(),
                eventType.getHumanReadableName(),
                eventType.getDescription(),
                subscriptions
        )).build();
    }

    @POST
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/types/system/show/{eventTypeName}/subscriptions")
    public Response subscribeActionToSystemEvent(@Context SecurityContext sc,
                                                 @PathParam("eventTypeName") @NotEmpty String eventTypeName,
                                                 @Valid SystemEventSubscriptionRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        SystemEventType eventType;
        try {
            eventType = SystemEventType.valueOf(eventTypeName.toUpperCase());
        } catch(IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        EventEngineImpl eventEngine = ((EventEngineImpl) nzyme.getEventEngine());

        // Pull action.
        Optional<EventActionEntry> action = eventEngine.findEventAction(req.actionId());

        if (action.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if orgadmin has access to event type.
        if (!authenticatedUser.isSuperAdministrator() && !eventType.getScope().equals(SystemEventScope.ORGANIZATION)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // Only superadmins can subscribe system/superadmin actions.
        if (!authenticatedUser.isSuperAdministrator() && action.get().organizationId() == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // Check if orgadmin has access to this action and can subscribe it.
        if (!authenticatedUser.isSuperAdministrator() && !action.get().organizationId()
                .equals(authenticatedUser.getOrganizationId())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // Check if this event already has this action ID subscribed to it.
        for (SubscriptionEntry sub : eventEngine
                .findAllActionsOfSubscription(authenticatedUser.getOrganizationId(), eventTypeName)) {
            if (sub.actionId().equals(action.get().uuid())) {
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.create("Action is already subscribed to this event."))
                        .build();
            }
        }

        eventEngine.subscribeActionToEvent(action.get().organizationId(), EventType.SYSTEM, eventType.name(), action.get().uuid());

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/types/system/show/{eventTypeName}/subscriptions/show/{subscriptionId}")
    public Response unsubscribeActionFromSystemEvent(@Context SecurityContext sc,
                                                     @PathParam("eventTypeName") @NotEmpty String eventTypeName,
                                                     @PathParam("subscriptionId") UUID subscriptionId) {
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
