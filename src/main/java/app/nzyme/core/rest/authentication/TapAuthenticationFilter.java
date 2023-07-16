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
import app.nzyme.core.security.authentication.db.TapPermissionEntry;
import com.google.common.net.HttpHeaders;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@TapSecured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class TapAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOG = LogManager.getLogger(TapAuthenticationFilter.class);

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    private final NzymeNode nzyme;

    public TapAuthenticationFilter(NzymeNode nzyme) {
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

            String tapSecret = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
            Optional<TapPermissionEntry> tapPermission = nzyme.getAuthenticationService().findTapBySecret(tapSecret);

            if (tapPermission.isPresent()) {
                // Set new security context for later use in resources.
                final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
                requestContext.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        TapPermissionEntry tpe = tapPermission.get();
                        return new AuthenticatedTap(tpe.uuid(), tpe.name(), tpe.organizationId(), tpe.tenantId());
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
            } else {
                LOG.warn("Rejected attempted authentication using invalid tap secret.");
                abortWithUnauthorized(requestContext);
            }
        } catch (Exception e) {
            LOG.info("Could not authenticate using tap secret.", e);
            abortWithUnauthorized(requestContext);
        }
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

}