package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.events.EventEngineImpl;
import app.nzyme.core.events.actions.EventActionUtilities;
import app.nzyme.core.events.actions.email.EmailActionConfiguration;
import app.nzyme.core.events.db.EventActionEntry;
import app.nzyme.core.events.types.EventActionType;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.CreateEmailEventActionRequest;
import app.nzyme.core.rest.requests.UpdateEmailEventActionRequest;
import app.nzyme.core.rest.responses.events.EventActionDetailsResponse;
import app.nzyme.core.rest.responses.events.EventActionsListResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/system/events/actions")
@Produces(MediaType.APPLICATION_JSON)
public class EventActionsResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(EventActionsResource.class);

    @Inject
    private NzymeNode nzyme;

    private final ObjectMapper om = new ObjectMapper();

    @GET
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
    public Response findAllActions(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        long total = ((EventEngineImpl) nzyme.getEventEngine()).countAllEventActionsOfAllOrganizations();
        List<EventActionDetailsResponse> events = Lists.newArrayList();
        for (EventActionEntry ea : ((EventEngineImpl) nzyme.getEventEngine())
                .findAllEventActionsOfAllOrganizations(limit, offset)) {
            events.add(EventActionUtilities.eventActionEntryToResponse(ea));
        }

        return Response.ok(EventActionsListResponse.create(total, events)).build();
    }

    @POST
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/email")
    public Response createEmailAction(@Context SecurityContext sc, @Valid CreateEmailEventActionRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        // Check permissions.
        if (!authenticatedUser.isSuperAdministrator()) {
            if (req.organizationId() == null || !req.organizationId().equals(authenticatedUser.getOrganizationId())) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }

        String config;
        try {
            config = om.writeValueAsString(EmailActionConfiguration.create(req.subjectPrefix(), req.receivers()));
        } catch (JsonProcessingException e) {
            LOG.error("Could not create action configuration.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        ((EventEngineImpl) nzyme.getEventEngine()).createEventAction(
                req.organizationId(),
                EventActionType.EMAIL,
                req.name(),
                req.description(),
                config
        );

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/email/{actionId}")
    public Response updateEmailAction(@Context SecurityContext sc,
                                      @Valid UpdateEmailEventActionRequest req,
                                      @PathParam("actionId") UUID actionId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        // Find action.
        Optional<EventActionEntry> action = ((EventEngineImpl) nzyme.getEventEngine()).findEventAction(actionId);

        if (action.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check permissions.
        if (!authenticatedUser.isSuperAdministrator()) {
            if (!action.get().organizationId().equals(authenticatedUser.getOrganizationId())) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }

        String config;
        try {
            config = om.writeValueAsString(EmailActionConfiguration.create(req.subjectPrefix(), req.receivers()));
        } catch (JsonProcessingException e) {
            LOG.error("Could not create action configuration.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        ((EventEngineImpl) nzyme.getEventEngine()).updateAction(
                action.get().uuid(),
                req.name(),
                req.description(),
                config
        );

        return Response.ok().build();
    }

}
