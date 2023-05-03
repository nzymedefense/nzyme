package app.nzyme.core.rest.resources.system.authentication.mgmt;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.requests.CreateUserRequest;
import app.nzyme.core.security.authentication.PasswordHasher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
                hash
        );

        return Response.status(Response.Status.CREATED).build();
    }

}
