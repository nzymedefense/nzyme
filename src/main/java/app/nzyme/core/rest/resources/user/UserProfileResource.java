package app.nzyme.core.rest.resources.user;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.events.types.SystemEvent;
import app.nzyme.core.events.types.SystemEventType;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.UpdateUserOwnPasswordRequest;
import app.nzyme.core.rest.responses.misc.ErrorResponse;
import app.nzyme.core.rest.responses.userprofile.UserProfileDetailsResponse;
import app.nzyme.core.security.authentication.PasswordHasher;
import app.nzyme.core.security.authentication.db.UserEntry;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.Map;
import java.util.Optional;

@Path("/api/user")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
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

        // System event.
        nzyme.getEventEngine().processEvent(SystemEvent.create(
                SystemEventType.AUTHENTICATION_PASSWORD_CHANGED,
                DateTime.now(),
                "Password of user [" + user.get().email() + "] was changed by same user."
        ), user.get().organizationId(), user.get().tenantId());

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

        // System event.
        nzyme.getEventEngine().processEvent(SystemEvent.create(
                SystemEventType.AUTHENTICATION_MFA_RESET,
                DateTime.now(),
                "MFA method of user [" + authenticatedUser.getEmail() + "] was reset by same user."
        ), authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        return Response.ok().build();
    }


}
