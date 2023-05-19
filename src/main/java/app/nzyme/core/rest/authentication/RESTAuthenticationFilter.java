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
import app.nzyme.core.security.authentication.roles.Permission;
import app.nzyme.core.security.authentication.roles.Permissions;
import app.nzyme.core.security.sessions.db.SessionEntry;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RESTSecured(PermissionLevel.ANY)
@Provider
@Priority(Priorities.AUTHENTICATION)
public class RESTAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOG = LogManager.getLogger(RESTAuthenticationFilter.class);

    private final NzymeNode nzyme;

    @Context
    ResourceInfo resourceInfo;

    @Context
    private javax.inject.Provider<Request> requestProvider;

    public static final String AUTHENTICATION_SCHEME = "Bearer";

    public RESTAuthenticationFilter(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Method resourceMethod = resourceInfo.getResourceMethod();
        Class resourceClass = resourceInfo.getResourceClass();

        RESTSecured methodAnnotation = resourceMethod.getAnnotation(RESTSecured.class);
        RESTSecured classAnnotation = (RESTSecured) resourceClass.getAnnotation(RESTSecured.class);

        PermissionLevel resourcePermissionLevel;
        String[] features = null;
        if (methodAnnotation == null && classAnnotation == null) {
            LOG.error("RESTSecured is NULL. Cannot continue. Resource: [/{}]",
                    requestContext.getUriInfo().getPath());
            abortWithUnauthorized(requestContext);
            return;
        } else {
            if (methodAnnotation == null) {
                resourcePermissionLevel = classAnnotation.value();
                features = classAnnotation.featurePermissions();
            } else {
                resourcePermissionLevel = methodAnnotation.value();
                features = methodAnnotation.featurePermissions();
            }
        }

        Optional<List<String>> requiredFeaturePermissions;
        if (features != null && features.length > 0) {
            requiredFeaturePermissions = Optional.of(Arrays.asList(features));
        } else {
            requiredFeaturePermissions = Optional.empty();
        }

        String remoteIp = "";
        if (requestProvider != null) { // For tests.
            final Request request = requestProvider.get();
            remoteIp = request.getHeader("X-Forwarded-For") == null
                    ? request.getRemoteAddr() : request.getHeader("X-Forwarded-For").split(",")[0];

            InetAddressValidator inetValidator = new InetAddressValidator();
            if (!inetValidator.isValid(remoteIp)) {
                LOG.warn("Invalid remote IP or X-Forwarded-For header in session request: [{}]. Aborting.", remoteIp);
                abortWithUnauthorized(requestContext);
            }
        }

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

            // Check if we have the permissions required by resource.
            switch (resourcePermissionLevel) {
                case SUPERADMINISTRATOR:
                    if (!user.get().isSuperAdmin()) {
                        LOG.warn("User <{}> requested resource [/{}] which requires super administrator permissions " +
                                        "but is not super administrator.",
                                user.get().email(), requestContext.getUriInfo().getPath());
                        abortWithUnauthorized(requestContext);
                        return;
                    }
                    break;
                case ORGADMINISTRATOR:
                    if (!user.get().isSuperAdmin() && !user.get().isOrganizationAdmin()) {
                        LOG.warn("User <{}> requested resource [/{}] which requires organization administrator permissions " +
                                        "but is not organization administrator.",
                                user.get().email(), requestContext.getUriInfo().getPath());
                        abortWithUnauthorized(requestContext);
                        return;
                    }
                    break;
                case ANY:
                    // Fine.
                    break;
            }

            // Check if we also need a feature permission.
            if (requiredFeaturePermissions.isPresent() && !user.get().isOrganizationAdmin() && !user.get().isSuperAdmin()) {
                List<String> userPermissions = nzyme.getAuthenticationService().findPermissionsOfUser(user.get().uuid());
                for (String requiredPermission : requiredFeaturePermissions.get()) {
                    if(!userPermissions.contains(requiredPermission)) {
                        LOG.warn("User <{}> requested resource [/{}] which requires missing feature permission [{}].",
                                user.get().email(), requestContext.getUriInfo().getPath(), requiredPermission);
                        abortWithUnauthorized(requestContext);
                        return;
                    }
                }
            }

            // Authenticated. Set last activity information.
            nzyme.getAuthenticationService().updateLastUserActivity(
                    user.get().uuid(),
                    remoteIp,
                    nzyme.getGeoIpService().lookup(InetAddress.getByName(remoteIp))
                            .orElse(null)
            );

            // Set new security context for later use in resources.
            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
            requestContext.setSecurityContext(new SecurityContext() {

                @Override
                public Principal getUserPrincipal() {
                    UserEntry u = user.get();
                    return new AuthenticatedUser(
                            u.uuid(),
                            session.get().sessionId(),
                            u.email(),
                            session.get().createdAt(),
                            u.organizationId(),
                            u.tenantId(),
                            u.isOrganizationAdmin(),
                            u.isSuperAdmin()
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