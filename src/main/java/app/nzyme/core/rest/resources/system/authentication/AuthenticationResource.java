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
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.authentication.RESTAuthenticationFilter;
import app.nzyme.core.rest.requests.MFAVerificationRequest;
import app.nzyme.core.rest.responses.authentication.MFAInitResponse;
import app.nzyme.core.rest.responses.authentication.SessionInformationResponse;
import app.nzyme.core.rest.responses.authentication.SessionTokenResponse;
import app.nzyme.core.rest.responses.authentication.SessionUserInformationDetailsResponse;
import app.nzyme.core.security.authentication.AuthenticationService;
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
import com.google.common.io.BaseEncoding;
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
import org.joda.time.DateTime;


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

        DateTime mfaExpiresAt = session.get().mfaRequestedAt() == null
                ? null : session.get().mfaRequestedAt().plusMinutes(AuthenticationService.MFA_ENTRY_TIME_MINUTES);

        return Response.ok(SessionInformationResponse.create(
                SessionUserInformationDetailsResponse.create(
                        user.get().id(),
                        user.get().email(),
                        user.get().name()
                ),
                session.get().mfaValid(),
                user.get().mfaComplete(),
                mfaExpiresAt
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

            // Encrypt.
            String encryptedUserSecret;
            String encryptedRecoveryCodesJson;
            try {
                encryptedUserSecret = BaseEncoding.base64().encode(
                        nzyme.getCrypto().encryptWithClusterKey(userSecret.getBytes())
                );
                encryptedRecoveryCodesJson = BaseEncoding.base64().encode(
                        nzyme.getCrypto().encryptWithClusterKey(recoveryCodesJson.getBytes())
                );
            } catch (Crypto.CryptoOperationException e) {
                LOG.error("Could not encrypt MFA data codes.", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            // Store encrypted data in database.
            nzyme.getAuthenticationService().setUserTOTPSecret(user.get().id(), encryptedUserSecret);
            nzyme.getAuthenticationService().setUserMFARecoveryCodes(user.get().id(), encryptedRecoveryCodesJson);
        } else {
            // User already has a secret (but MFA setup not complete. Aborted wizard?) Use existing secret.
            try {
                userSecret = new String(nzyme.getCrypto().decryptWithClusterKey(
                        BaseEncoding.base64().decode(user.get().totpSecret())
                ));
            } catch (Crypto.CryptoOperationException e) {
                LOG.error("Could not decrypt MFA data codes.", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            try {
                String recoveryCodesDecryptedJson;
                try {
                    recoveryCodesDecryptedJson = new String(nzyme.getCrypto().decryptWithClusterKey(
                            BaseEncoding.base64().decode(user.get().mfaRecoveryCodes())
                    ));
                } catch (Crypto.CryptoOperationException e) {
                    LOG.error("Could not decrypt MFA data codes.", e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }

                recoveryCodes = new ObjectMapper().readValue(recoveryCodesDecryptedJson, new TypeReference<>() {});
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

        if (user.isEmpty() || !user.get().mfaComplete()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Compare codes.
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        String userSecret;
        try {
            userSecret = new String(nzyme.getCrypto().decryptWithClusterKey(
                    BaseEncoding.base64().decode(user.get().totpSecret())
            ));
        } catch (Crypto.CryptoOperationException e) {
            LOG.error("Could not decrypt MFA data codes.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (!verifier.isValidCode(userSecret, req.code())) {
            LOG.info("User <{}> failed MFA challenge.", user.get().email());
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // We have a valid TOTP. Mark session as MFA'd.
        LOG.info("User <{}> passed MFA challenge.", user.get().email());
        nzyme.getAuthenticationService().markSessionAsMFAValid(session.get().sessionId());

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