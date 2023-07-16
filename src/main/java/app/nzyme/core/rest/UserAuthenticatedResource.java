package app.nzyme.core.rest;

import app.nzyme.core.rest.authentication.AuthenticatedUser;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(UserAuthenticatedResource.class);

    protected AuthenticatedUser getAuthenticatedUser(SecurityContext sc) {
        return (AuthenticatedUser) sc.getUserPrincipal();
    }

}
