package app.nzyme.core.rest.resources.user;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.userprofile.UserProfileDetailsResponse;
import app.nzyme.core.security.authentication.db.UserEntry;
import app.nzyme.plugin.rest.security.RESTSecured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Optional;

@Path("/api/user")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured
public class UserProfileResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(UserProfileResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/profile")
    public Response findOwnProfile(@Context SecurityContext sc) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserById(authenticatedUser.getUserId());

        if (user.isEmpty()) {
            LOG.error("User <{}> referenced in session does not exist.", authenticatedUser.getUserId());
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(UserProfileDetailsResponse.create(user.get().email(), user.get().name())).build();
    }

    @GET
    @Path("/mfa/recoverycodes")
    public Response findOwnMfaRecoveryCodes(@Context SecurityContext sc) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<List<String>> recoveryCodes = nzyme.getAuthenticationService()
                .getUserMFARecoveryCodes(authenticatedUser.getUserId());

        if (recoveryCodes.isEmpty()) {
            LOG.error("No recovery codes for user <{}> found.", authenticatedUser.getUserId());
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(recoveryCodes.get()).build();
    }

    @PUT
    @Path("/mfa/reset")
    public Response resetOwnMfa(@Context SecurityContext sc) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        nzyme.getAuthenticationService().resetMFAOfUser(authenticatedUser.getUserId());

        return Response.ok().build();
    }


}
