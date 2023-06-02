package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.events.EventEngineImpl;
import app.nzyme.core.events.db.EventEntry;
import app.nzyme.core.events.types.SystemEventScope;
import app.nzyme.core.events.types.SystemEventType;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.events.EventDetailsResponse;
import app.nzyme.core.rest.responses.events.EventTypeDetailsResponse;
import app.nzyme.core.rest.responses.events.EventTypesListResponse;
import app.nzyme.core.rest.responses.events.EventsListResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/system/events")
@Produces(MediaType.APPLICATION_JSON)
public class EventsResource extends UserAuthenticatedResource {

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

        List<EventTypeDetailsResponse> result = Lists.newArrayList();
        for (SystemEventType entry : page) {
            result.add(EventTypeDetailsResponse.create(
                    entry.name(),
                    entry.getCategory().name(),
                    entry.getCategory().getHumanReadableName(),
                    entry.getHumanReadableName(),
                    entry.getDescription(),
                    0L // TODO count descriptions
            ));
        }

        return Response.ok(EventTypesListResponse.create(totalEvents, result)).build();
    }

}
