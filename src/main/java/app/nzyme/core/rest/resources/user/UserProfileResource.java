package app.nzyme.core.rest.resources.user;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.UpdateUserOwnPasswordRequest;
import app.nzyme.core.rest.responses.misc.ErrorResponse;
import app.nzyme.core.rest.responses.userprofile.UserProfileDetailsResponse;
import app.nzyme.core.security.authentication.PasswordHasher;
import app.nzyme.core.security.authentication.db.UserEntry;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;
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

    @PUT
    @Path("/password")
    public Response changeOwnPassword(@Context SecurityContext sc, UpdateUserOwnPasswordRequest r) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (Strings.isNullOrEmpty(r.currentPassword()) || Strings.isNullOrEmpty(r.newPassword())) {
             return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserById(authenticatedUser.getUserId());

        if (user.isEmpty()) {
            LOG.error("User <{}> referenced in session does not exist.", authenticatedUser.getUserId());
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        PasswordHasher hasher = new PasswordHasher(nzyme.getMetrics());

        // Compare old password.
        try {
            if (!hasher.compareHash(r.currentPassword(), user.get().passwordHash(), user.get().passwordSalt())) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorResponse.create("Incorrect current password.")).build();
            }
        } catch(Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ErrorResponse.create("Incorrect current password.")).build();
        }

        PasswordHasher.GeneratedHashAndSalt newPasswordHash = hasher.createHash(r.newPassword());

        nzyme.getAuthenticationService().editUserPassword(
                user.get().uuid(),
                newPasswordHash
        );

        // Invalidate session of user.
        nzyme.getAuthenticationService().deleteAllSessionsOfUser(user.get().uuid());

        return Response.ok().build();
    }

    @GET
    @Path("/mfa/recoverycodes")
    public Response findOwnMfaRecoveryCodes(@Context SecurityContext sc) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<Map<String, Boolean>> recoveryCodes = nzyme.getAuthenticationService()
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
