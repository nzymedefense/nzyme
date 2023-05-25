package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.events.db.EventEntry;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.system.EventDetailsResponse;
import app.nzyme.core.rest.responses.system.EventsListResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

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

@Path("/api/system/events")
@RESTSecured(PermissionLevel.ORGADMINISTRATOR)
@Produces(MediaType.APPLICATION_JSON)
public class EventsResource extends UserAuthenticatedResource {

    @Inject
    private NzymeNode nzyme;

    @GET
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
            events = nzyme.getEventEngine().findAllEventsOfAllOrganizations(types, limit, offset);
            totalEvents = nzyme.getEventEngine().countAllEventsOfAllOrganizations();
        } else {
            // Organization admin.
            events = nzyme.getEventEngine()
                    .findAllEventsOfOrganization(types, authenticatedUser.getOrganizationId(), limit, offset);
            totalEvents = nzyme.getEventEngine().countAllEventsOfOrganization(authenticatedUser.getOrganizationId());
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

}
