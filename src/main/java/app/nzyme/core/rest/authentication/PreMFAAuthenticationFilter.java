package app.nzyme.core.rest.authentication;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.security.authentication.db.UserEntry;
import app.nzyme.core.security.sessions.db.SessionEntry;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.net.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@PreMFASecured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class PreMFAAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOG = LogManager.getLogger(PreMFAAuthenticationFilter.class);

    private final NzymeNode nzyme;

    public static final String AUTHENTICATION_SCHEME = "Bearer";

    public PreMFAAuthenticationFilter(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

            if (!isTokenBasedAuthentication(authorizationHeader)) {
                abortWithUnauthorized(requestContext);
                return;
            }

            String sessionId = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

            // Check if session exists.
            Optional<SessionEntry> session = nzyme.getAuthenticationService().findSessionWithOrWithoutPassedMFABySessionId(sessionId);
            if (session.isEmpty()) {
                abortWithUnauthorized(requestContext);
                return;
            }

            Optional<UserEntry> user = nzyme.getAuthenticationService().findUserById(session.get().userId());

            if (user.isEmpty()) {
                LOG.error("Session referenced user that doesn't exist. Aborting.");
                abortWithUnauthorized(requestContext);
                return;
            }

            // Set new security context for later use in resources.
            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
            requestContext.setSecurityContext(new SecurityContext() {

                @Override
                public Principal getUserPrincipal() {
                    return new AuthenticatedUser(
                            user.get().id(),
                            session.get().sessionId(),
                            user.get().email(),
                            session.get().createdAt(),
                            user.get().organizationId(),
                            user.get().tenantId()
                    );
                }

                @Override
                public boolean isUserInRole(String role) {
                    return true;
                }

                @Override
                public boolean isSecure() {
                    return currentSecurityContext.isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return AUTHENTICATION_SCHEME;
                }

            });
        } catch (Exception e) {
            LOG.warn("Session ID validation failed.", e);
            abortWithUnauthorized(requestContext);
            return;
        }
    }

    public static boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }

}
