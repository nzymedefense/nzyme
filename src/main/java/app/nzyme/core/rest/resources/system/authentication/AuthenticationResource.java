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

package app.nzyme.core.rest.resources.system.authentication;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.authentication.RESTAuthenticationFilter;
import app.nzyme.core.rest.requests.MFAVerificationRequest;
import app.nzyme.core.rest.responses.authentication.MFAInitResponse;
import app.nzyme.core.rest.responses.authentication.SessionInformationResponse;
import app.nzyme.core.rest.responses.authentication.SessionTokenResponse;
import app.nzyme.core.security.authentication.PasswordHasher;
import app.nzyme.core.security.authentication.db.UserEntry;
import app.nzyme.core.security.sessions.SessionId;
import app.nzyme.core.security.sessions.db.SessionEntry;
import app.nzyme.plugin.rest.security.RESTSecured;
import app.nzyme.core.rest.requests.CreateSessionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Optional;

@Path("/api/system/authentication")
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(AuthenticationResource.class);

    @Inject
    private NzymeNode nzyme;

    @Context
    SecurityContext securityContext;

    @POST
    @Path("/session")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSession(@Context org.glassfish.grizzly.http.server.Request rc,
                                  @NotNull CreateSessionRequest request) {
        String remoteIp = rc.getHeader("X-Forwarded-For") == null
                ? rc.getRemoteAddr() : rc.getHeader("X-Forwarded-For").split(",")[0];

        InetAddressValidator inetValidator = new InetAddressValidator();
        if (!inetValidator.isValid(remoteIp)) {
            LOG.warn("Invalid remote IP or X-Forwarded-For header in session request: [{}]. Aborting.", remoteIp);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String username = request.username();
        String password = request.password();

        // Pull user this login impersonates.
        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserByEmail(request.username());

        // Verify hash.
        PasswordHasher hasher = new PasswordHasher(nzyme.getMetrics());

        if (!hasher.runPasswordPreconditions(request.password())) {
            LOG.warn("Failed login attempt for user [{}]. (Password preconditions not met.)", username);
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        String hash;
        String salt;
        if (user.isPresent()) {
            // User found.
            hash = user.get().passwordHash();
            salt = user.get().passwordSalt();
        } else {
            /*
             * No such user. Instead of returning immediately, create a new hash/salt that will not match to make
             * timing attacks harder.
             */
            PasswordHasher.GeneratedHashAndSalt generated = hasher.createHash(
                    RandomStringUtils.random(18, true, true)
            );

            hash = generated.hash();
            salt = generated.salt();
        }

        if (hasher.compareHash(password, hash, salt)) {
            // Correct password. Create session.
            String sessionId = SessionId.createSessionId();

            // TODO does user already have a session? Delete all sessions of user.

            nzyme.getAuthenticationService().deleteAllSessionsOfUser(user.get().id());
            nzyme.getAuthenticationService().createSession(sessionId, user.get().id(), remoteIp);

            LOG.info("Creating session for user [{}]", username);
            return Response.status(Response.Status.CREATED).entity(SessionTokenResponse.create(sessionId)).build();
        } else {
            LOG.warn("Failed login attempt for user [{}].", username);
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @GET
    @Path("/session")
    public Response getSessionInformation(@Context ContainerRequestContext requestContext) {
        /*
         * The @RestSecured filter is not going to work because it will not let non-MFA'd sessions pass, but we
         * need those.here. Manually pulling the session ID out of the header.
         */
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (!RESTAuthenticationFilter.isTokenBasedAuthentication(authorizationHeader)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String sessionId = authorizationHeader.substring(RESTAuthenticationFilter.AUTHENTICATION_SCHEME.length()).trim();

        Optional<SessionEntry> session = nzyme.getAuthenticationService().findSessionWithOrWithoutPassedMFABySessionId(sessionId);

        if (session.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserById(session.get().userId());

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(SessionInformationResponse.create(
                session.get().mfaValid(),
                user.get().mfaComplete()
        )).build();
    }

    @GET
    @Path("/mfa/setup/initialize")
    public Response initializeMfaSetup(@Context ContainerRequestContext requestContext) {
        /*
         * The @RestSecured filter is not going to work because it will not let non-MFA'd sessions pass, but we
         * need those.here. Manually pulling the session ID out of the header.
         */
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (!RESTAuthenticationFilter.isTokenBasedAuthentication(authorizationHeader)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String sessionId = authorizationHeader.substring(RESTAuthenticationFilter.AUTHENTICATION_SCHEME.length()).trim();

        Optional<SessionEntry> session = nzyme.getAuthenticationService().findSessionWithOrWithoutPassedMFABySessionId(sessionId);

        if (session.isEmpty() || session.get().mfaValid()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserById(session.get().userId());

        if (user.isEmpty() || user.get().mfaComplete()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String userSecret;
        List<String> recoveryCodes;
        if (Strings.isNullOrEmpty(user.get().totpSecret())) {
            // Store secret and recovery codes with user.
            SecretGenerator secretGenerator = new DefaultSecretGenerator();
            RecoveryCodeGenerator recoveryCodeGenerator = new RecoveryCodeGenerator();

            userSecret = secretGenerator.generate();
            recoveryCodes = Lists.newArrayList(recoveryCodeGenerator.generateCodes(8));

            String recoveryCodesJson;
            try {
                recoveryCodesJson = new ObjectMapper().writeValueAsString(recoveryCodes);
            } catch (JsonProcessingException e) {
                LOG.error("Could not serialize MFA recovery codes.", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            nzyme.getAuthenticationService().setUserTOTPSecret(user.get().id(), userSecret);
            nzyme.getAuthenticationService().setUserMFARecoveryCodes(user.get().id(), recoveryCodesJson);
        } else {
            // User already has a secret (but MFA setup not complete. Aborted wizard?) Use existing secret.
            userSecret = user.get().totpSecret();

            try {
                recoveryCodes = new ObjectMapper().readValue(user.get().mfaRecoveryCodes(), new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                LOG.error("Could not deserialize MFA recovery codes.", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        return Response.ok(MFAInitResponse.create(userSecret, user.get().email(), recoveryCodes)).build();
    }

    @POST
    @Path("/mfa/setup/complete")
    public Response completeMfaSetup(@Context ContainerRequestContext requestContext) {
        /*
         * The @RestSecured filter is not going to work because it will not let non-MFA'd sessions pass, but we
         * need those.here. Manually pulling the session ID out of the header.
         */
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (!RESTAuthenticationFilter.isTokenBasedAuthentication(authorizationHeader)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String sessionId = authorizationHeader.substring(RESTAuthenticationFilter.AUTHENTICATION_SCHEME.length()).trim();

        Optional<SessionEntry> session = nzyme.getAuthenticationService().findSessionWithOrWithoutPassedMFABySessionId(sessionId);

        if (session.isEmpty() || session.get().mfaValid()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserById(session.get().userId());

        if (user.isEmpty() || user.get().mfaComplete()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().setUserMFAComplete(user.get().id(), true);

        return Response.ok().build();
    }


    @POST
    @Path("/mfa/verify")
    public Response verifyMfa(@Context ContainerRequestContext requestContext, MFAVerificationRequest req) {
        /*
         * The @RestSecured filter is not going to work because it will not let non-MFA'd sessions pass, but we
         * need those.here. Manually pulling the session ID out of the header.
         */
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (!RESTAuthenticationFilter.isTokenBasedAuthentication(authorizationHeader)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String sessionId = authorizationHeader.substring(RESTAuthenticationFilter.AUTHENTICATION_SCHEME.length()).trim();

        Optional<SessionEntry> session = nzyme.getAuthenticationService().findSessionWithOrWithoutPassedMFABySessionId(sessionId);

        if (session.isEmpty() || session.get().mfaValid()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserById(session.get().userId());

        if (user.isEmpty() || user.get().mfaComplete()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Compare codes.
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        if (!verifier.isValidCode(user.get().totpSecret(), req.code())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // Set session to mfa valid etc.
        LOG.info("MFA VALID!!");

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured
    @Path("/session")
    public Response deleteSession(@Context SecurityContext sc) {
        AuthenticatedUser user = getAuthenticatedUser(sc);
        nzyme.getAuthenticationService().deleteAllSessionsOfUser(user.getUserId());

        LOG.info("Deleting session of user [{}].", user.getEmail());

        return Response.ok().build();
    }

}
