package app.nzyme.core.rest.resources.system.authentication.mgmt;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.requests.CreateUserRequest;
import app.nzyme.core.security.authentication.PasswordHasher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/system/authentication/mgmt/initialuser")
@Produces(MediaType.APPLICATION_JSON)
public class InitialUserResource {

    private static final Logger LOG = LogManager.getLogger(InitialUserResource.class);

    @Inject
    private NzymeNode nzyme;

    @POST
    public Response createInitialUser(CreateUserRequest req) {
        if (nzyme.getAuthenticationService().countSuperAdministrators() > 0) {
            LOG.warn("Attempt to access initial user creation but users already exist.");
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        if (!OrganizationsResource.validateCreateUserRequest(req)) {
            LOG.info("Invalid parameters in create initial user request.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        PasswordHasher hasher = new PasswordHasher(nzyme.getMetrics());
        PasswordHasher.GeneratedHashAndSalt hash = hasher.createHash(req.password());

        LOG.info("Creating initial user [{}].", req.email());

        nzyme.getAuthenticationService().createSuperAdministrator(
                req.name(),
                req.email().toLowerCase(),
                req.disableMfa(),
                hash
        );

        return Response.status(Response.Status.CREATED).build();
    }

}
