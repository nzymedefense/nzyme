package app.nzyme.core.rest;

import app.nzyme.core.rest.authentication.AuthenticatedUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.SecurityContext;

public class UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(UserAuthenticatedResource.class);

    protected AuthenticatedUser getAuthenticatedUser(SecurityContext sc) {
        return (AuthenticatedUser) sc.getUserPrincipal();
    }

}
