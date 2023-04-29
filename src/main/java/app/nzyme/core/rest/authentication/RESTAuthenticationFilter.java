/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

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

@RESTSecured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class RESTAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOG = LogManager.getLogger(RESTAuthenticationFilter.class);

    private final NzymeNode nzyme;

    public static final String AUTHENTICATION_SCHEME = "Bearer";

    public RESTAuthenticationFilter(NzymeNode nzyme) {
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
            Optional<SessionEntry> session = nzyme.getAuthenticationService().findSessionWithPassedMFABySessionId(sessionId);
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

            // Authenticated. Set last activity information.
            nzyme.getAuthenticationService().updateLastUserActivity(user.get().id());

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