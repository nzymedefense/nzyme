package app.nzyme.core.rest.resources.system.authentication.mgmt;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.rest.requests.*;
import app.nzyme.core.rest.responses.authentication.SessionDetailsResponse;
import app.nzyme.core.rest.responses.authentication.SessionsListResponse;
import app.nzyme.core.rest.responses.authentication.mgmt.*;
import app.nzyme.core.rest.responses.misc.ErrorResponse;
import app.nzyme.core.security.authentication.PasswordHasher;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TapPermissionEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.core.security.authentication.db.UserEntry;
import app.nzyme.core.security.sessions.db.SessionEntryWithUserDetails;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/system/authentication/mgmt/organizations")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class OrganizationsResource {

    private static final Logger LOG = LogManager.getLogger(OrganizationsResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response findAll() {
        List<OrganizationDetailsResponse> organizations = Lists.newArrayList();

        for (OrganizationEntry org : nzyme.getAuthenticationService().findAllOrganizations()) {
            organizations.add(organizationEntryToResponse(org));
        }

        return Response.ok(OrganizationsListResponse.create(organizations)).build();
    }

    @GET
    @Path("/show/{id}")
    public Response find(@PathParam("id") long id) {
        if (id <= 0) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(id);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(organizationEntryToResponse(org.get())).build();
    }

    @POST
    public Response create(CreateOrganizationRequest req) {
        if (req.name().trim().isEmpty() || req.description().trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        nzyme.getAuthenticationService().createOrganization(req.name(), req.description());

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/show/{id}")
    public Response update(@PathParam("id") long id, UpdateOrganizationRequest req) {
        if (id <= 0) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(id);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().updateOrganization(id, req.name(), req.description());

        return Response.ok().build();
    }

    @DELETE
    @Path("/show/{id}")
    public Response delete(@PathParam("id") long id) {
        if (id <= 0) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(id);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!nzyme.getAuthenticationService().isOrganizationDeletable(org.get())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        nzyme.getAuthenticationService().deleteOrganization(id);

        return Response.ok().build();
    }

    @GET
    @Path("/show/{organizationId}/tenants")
    public Response findTenantsOfOrganization(@PathParam("organizationId") long organizationId) {
        if (organizationId <= 0) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(organizationId);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<List<TenantEntry>> tenants = nzyme.getAuthenticationService().findAllTenantsOfOrganization(org.get().id());

        List<TenantDetailsResponse> response = Lists.newArrayList();
        if (tenants.isPresent()) {
            for (TenantEntry tenant : tenants.get()) {
                response.add(tenantEntryToResponse(tenant));
            }
        }

        return Response.ok(TenantsListResponse.create(response)).build();
    }

    @GET
    @Path("/show/{organizationId}/tenants/show/{tenantId}")
    public Response findTenantOfOrganization(@PathParam("organizationId") long organizationId,
                                             @PathParam("tenantId") long tenantId) {
        if (organizationId <= 0 || tenantId <= 0) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(organizationId);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<TenantEntry> tenant = nzyme.getAuthenticationService().findTenant(tenantId);

        if (tenant.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(tenantEntryToResponse(tenant.get())).build();
    }

    @POST
    @Path("/show/{organizationId}/tenants/")
    public Response createTenant(@PathParam("organizationId") long organizationId, CreateTenantRequest req) {
        if (req.name().trim().isEmpty() || req.description().trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!organizationExists(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().createTenant(organizationId, req.name(), req.description());

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/show/{organizationId}/tenants/show/{tenantId}")
    public Response updateTenant(@PathParam("organizationId") long organizationId,
                                 @PathParam("tenantId") long tenantId,
                                 UpdateTenantRequest req) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().updateTenant(tenantId, req.name(), req.description());

        return Response.ok().build();
    }

    @DELETE
    @Path("/show/{organizationId}/tenants/show/{tenantId}")
    public Response deleteTenant(@PathParam("organizationId") long organizationId,
                                 @PathParam("tenantId") long tenantId) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().deleteTenant(tenantId);

        return Response.ok().build();
    }

    @GET
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users")
    public Response findAllUsersOfTenant(@PathParam("organizationId") long organizationId,
                                         @PathParam("tenantId") long tenantId) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<UserDetailsResponse> users = Lists.newArrayList();
        for (UserEntry user : nzyme.getAuthenticationService().findAllUsersOfTenant(organizationId, tenantId)) {
            users.add(userEntryToResponse(user));
        }

        return Response.ok(UsersListResponse.create(users)).build();
    }

    @GET
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}")
    public Response findUserOfTenant(@PathParam("organizationId") long organizationId,
                                     @PathParam("tenantId") long tenantId,
                                     @PathParam("userId") long userId) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(userEntryToResponse(user.get())).build();
    }

    @POST
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users")
    public Response createUserOfTenant(@PathParam("organizationId") long organizationId,
                                       @PathParam("tenantId") long tenantId,
                                       CreateUserRequest req) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!validateCreateUserRequest(req)) {
            LOG.info("Invalid parameters in create user request.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (nzyme.getAuthenticationService().userWithEmailExists(req.email().toLowerCase())) {
            LOG.info("User with email address already exists.");
            return Response.status(Response.Status.UNAUTHORIZED).entity(
                    ErrorResponse.create("Email address already in use.")
            ).build();
        }

        PasswordHasher hasher = new PasswordHasher(nzyme.getMetrics());
        PasswordHasher.GeneratedHashAndSalt hash = hasher.createHash(req.password());

        nzyme.getAuthenticationService().createUserOfTenant(
                organizationId,
                tenantId,
                req.name(),
                req.email().toLowerCase(),
                hash
        );

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}")
    public Response editUserOfTenant(@PathParam("organizationId") long organizationId,
                                     @PathParam("tenantId") long tenantId,
                                     @PathParam("userId") long userId,
                                     UpdateUserRequest req) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!validateUpdateUserRequest(req)) {
            LOG.info("Invalid parameters in update user request.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!user.get().email().equals(req.email()) && nzyme.getAuthenticationService().userWithEmailExists(
                req.email().toLowerCase())) {
            LOG.info("User with email address already exists.");
            return Response.status(Response.Status.UNAUTHORIZED).entity(
                    ErrorResponse.create("Email address already in use.")
            ).build();
        }

        nzyme.getAuthenticationService().editUserOfTenant(
                organizationId,
                tenantId,
                userId,
                req.name(),
                req.email().toLowerCase()
        );

        return Response.ok().build();
    }

    @DELETE
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}")
    public Response deleteUserOfTenant(@PathParam("organizationId") long organizationId,
                                       @PathParam("tenantId") long tenantId,
                                       @PathParam("userId") long userId) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().deleteUserOfTenant(organizationId, tenantId, userId);

        return Response.ok().build();
    }

    @PUT
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}/password")
    public Response editUserOfTenantPassword(@PathParam("organizationId") long organizationId,
                                             @PathParam("tenantId") long tenantId,
                                             @PathParam("userId") long userId,
                                             UpdatePasswordRequest req) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!validateUpdatePasswordRequest(req)) {
            LOG.info("Invalid password in update password request.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        PasswordHasher hasher = new PasswordHasher(nzyme.getMetrics());
        PasswordHasher.GeneratedHashAndSalt hash = hasher.createHash(req.password());

        nzyme.getAuthenticationService().editUserOfTenantPassword(
                organizationId,
                tenantId,
                userId,
                hash
        );

        return Response.ok().build();
    }

    @GET
    @Path("/sessions")
    public Response findAllSessions(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<SessionDetailsResponse> sessions = Lists.newArrayList();
        for (SessionEntryWithUserDetails session : nzyme.getAuthenticationService().findAllSessions(limit, offset)) {
            sessions.add(SessionDetailsResponse.create(
                    session.id(),
                    session.organizationId(),
                    session.tenantId(),
                    session.userId(),
                    session.userEmail(),
                    session.userName(),
                    session.isSuperadmin(),
                    session.isOrgadmin(),
                    session.remoteIp(),
                    session.createdAt(),
                    session.lastActivity()
            ));
        }

        long sessionCount = nzyme.getAuthenticationService().countAllSessions();

        return Response.ok(SessionsListResponse.create(sessionCount, sessions)).build();
    }

    @GET
    @Path("/show/{organizationId}/sessions")
    public Response findSessionsOfOrganization(@PathParam("organizationId") long organizationId,
                                               @QueryParam("limit") int limit,
                                               @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (organizationId <= 0) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(organizationId);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<SessionDetailsResponse> sessions = Lists.newArrayList();
        for (SessionEntryWithUserDetails session : nzyme.getAuthenticationService().findSessionsOfOrganization(
                organizationId, limit, offset)) {
            sessions.add(SessionDetailsResponse.create(
                    session.id(),
                    session.organizationId(),
                    session.tenantId(),
                    session.userId(),
                    session.userEmail(),
                    session.userName(),
                    session.isSuperadmin(),
                    session.isOrgadmin(),
                    session.remoteIp(),
                    session.createdAt(),
                    session.lastActivity()
            ));
        }

        long sessionCount = nzyme.getAuthenticationService().countSessionsOfOrganization(organizationId);

        return Response.ok(SessionsListResponse.create(sessionCount, sessions)).build();
    }

    @GET
    @Path("/show/{organizationId}/tenants/show/{tenantId}/sessions")
    public Response findSessionsOfTenant(@PathParam("organizationId") long organizationId,
                                         @PathParam("tenantId") long tenantId,
                                         @QueryParam("limit") int limit,
                                         @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<SessionDetailsResponse> sessions = Lists.newArrayList();
        for (SessionEntryWithUserDetails session : nzyme.getAuthenticationService().findSessionsOfTenant(
                organizationId, tenantId, limit, offset)) {
            sessions.add(SessionDetailsResponse.create(
                    session.id(),
                    session.organizationId(),
                    session.tenantId(),
                    session.userId(),
                    session.userEmail(),
                    session.userName(),
                    session.isSuperadmin(),
                    session.isOrgadmin(),
                    session.remoteIp(),
                    session.createdAt(),
                    session.lastActivity()
            ));
        }

        long sessionCount = nzyme.getAuthenticationService().countSessionsOfTenant(organizationId, tenantId);

        return Response.ok(SessionsListResponse.create(sessionCount, sessions)).build();
    }


    @DELETE
    @Path("/sessions/show/{sessionId}")
    public Response invalidateSession(@PathParam("sessionId") long sessionId) {
        nzyme.getAuthenticationService().deleteSession(sessionId);

        return Response.ok().build();
    }

    @GET
    @Path("/show/{organizationId}/tenants/show/{tenantId}/taps")
    public Response findAllTaps(@PathParam("organizationId") long organizationId,
                                @PathParam("tenantId") long tenantId) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<TapPermissionDetailsResponse> taps = Lists.newArrayList();
        for (TapPermissionEntry tap : nzyme.getAuthenticationService().findAllTaps(organizationId, tenantId)) {
            taps.add(tapPermissionEntryToResponse(tap));
        }

        return Response.ok(TapPermissionsListResponse.create(taps)).build();
    }

    @GET
    @Path("/show/{organizationId}/tenants/show/{tenantId}/taps/show/{tapUuid}")
    public Response findTap(@PathParam("organizationId") long organizationId,
                            @PathParam("tenantId") long tenantId,
                            @PathParam("tapUuid") String tapUuid) {
        UUID tapId;
        try {
            tapId = UUID.fromString(tapUuid);
        } catch(IllegalArgumentException ignored) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<TapPermissionEntry> tap = nzyme.getAuthenticationService().findTap(organizationId, tenantId, tapId);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(tapPermissionEntryToResponse(tap.get())).build();
    }

    @POST
    @Path("/show/{organizationId}/tenants/show/{tenantId}/taps")
    public Response createTap(@PathParam("organizationId") long organizationId,
                              @PathParam("tenantId") long tenantId,
                              CreateTapRequest req) {
        if (req.name().trim().isEmpty() || req.description().trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String secret = RandomStringUtils.random(64, true, true);

        nzyme.getAuthenticationService().createTap(
                organizationId,
                tenantId,
                secret,
                req.name(),
                req.description()
        );

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/show/{organizationId}/tenants/show/{tenantId}/taps/show/{tapUuid}")
    public Response editTap(@PathParam("organizationId") long organizationId,
                            @PathParam("tenantId") long tenantId,
                            @PathParam("tapUuid") String tapUuid,
                            UpdateTapRequest req) {
        UUID tapId;
        try {
            tapId = UUID.fromString(tapUuid);
        } catch(IllegalArgumentException ignored) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<TapPermissionEntry> tap = nzyme.getAuthenticationService().findTap(organizationId, tenantId, tapId);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().editTap(organizationId, tenantId, tapId, req.name(), req.description());

        return Response.ok().build();
    }

    @DELETE
    @Path("/show/{organizationId}/tenants/show/{tenantId}/taps/show/{tapUuid}")
    public Response deleteTap(@PathParam("organizationId") long organizationId,
                            @PathParam("tenantId") long tenantId,
                            @PathParam("tapUuid") String tapUuid) {
        UUID tapId;
        try {
            tapId = UUID.fromString(tapUuid);
        } catch(IllegalArgumentException ignored) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<TapPermissionEntry> tap = nzyme.getAuthenticationService().findTap(organizationId, tenantId, tapId);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().deleteTap(organizationId, tenantId, tapId);

        return Response.ok().build();
    }

    @PUT
    @Path("/show/{organizationId}/tenants/show/{tenantId}/taps/show/{tapUuid}/secret/cycle")
    public Response cycleTapSecret(@PathParam("organizationId") long organizationId,
                                   @PathParam("tenantId") long tenantId,
                                   @PathParam("tapUuid") String tapUuid) {
        UUID tapId;
        try {
            tapId = UUID.fromString(tapUuid);
        } catch(IllegalArgumentException ignored) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<TapPermissionEntry> tap = nzyme.getAuthenticationService().findTap(organizationId, tenantId, tapId);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String newSecret = RandomStringUtils.random(64, true, true);
        nzyme.getAuthenticationService().cycleTapSecret(organizationId, tenantId, tapId, newSecret);

        return Response.ok().build();
    }

    private OrganizationDetailsResponse organizationEntryToResponse(OrganizationEntry org) {
        return OrganizationDetailsResponse.create(
                org.id(),
                org.name(),
                org.description(),
                org.createdAt(),
                org.updatedAt(),
                nzyme.getAuthenticationService().countTenantsOfOrganization(org),
                nzyme.getAuthenticationService().countUsersOfOrganization(org),
                nzyme.getAuthenticationService().countTapsOfOrganization(org),
                nzyme.getAuthenticationService().isOrganizationDeletable(org)
        );
    }

    private TenantDetailsResponse tenantEntryToResponse(TenantEntry t) {
        return TenantDetailsResponse.create(
                t.id(),
                t.organizationId(),
                t.name(),
                t.description(),
                t.createdAt(),
                t.updatedAt(),
                nzyme.getAuthenticationService().countUsersOfTenant(t),
                nzyme.getAuthenticationService().countTapsOfTenant(t),
                nzyme.getAuthenticationService().isTenantDeletable(t)
        );
    }

    private UserDetailsResponse userEntryToResponse(UserEntry u) {
        return UserDetailsResponse.create(
                u.id(),
                u.organizationId(),
                u.tenantId(),
                u.roleId(),
                u.email(),
                u.name(),
                u.createdAt(),
                u.updatedAt(),
                u.lastActivity()
        );
    }

    private TapPermissionDetailsResponse tapPermissionEntryToResponse(TapPermissionEntry tpe) {
        String decryptedSecret;
        try {
            decryptedSecret = new String(nzyme.getCrypto().decryptWithClusterKey(Base64.decode(tpe.secret())));
        } catch (Crypto.CryptoOperationException e) {
            throw new RuntimeException("Could not decrypt tap secret.", e);
        }

        return TapPermissionDetailsResponse.create(
                tpe.uuid(),
                tpe.organizationId(),
                tpe.tenantId(),
                tpe.name(),
                tpe.description(),
                decryptedSecret,
                tpe.createdAt(),
                tpe.updatedAt(),
                tpe.lastReport()
        );
    }

    private boolean organizationExists(long organizationId) {
        if (organizationId <= 0) {
            return false;
        }

        if (nzyme.getAuthenticationService().findOrganization(organizationId).isEmpty()) {
            return false;
        }

        return true;
    }

    private boolean organizationAndTenantExists(long organizationId, long tenantId) {
        if (organizationId <= 0 || tenantId <= 0) {
            return false;
        }

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(organizationId);

        if (org.isEmpty()) {
            return false;
        }

        Optional<TenantEntry> tenant = nzyme.getAuthenticationService().findTenant(tenantId);

        if (tenant.isEmpty()) {
            return false;
        }

        return true;
    }

    public static boolean validateCreateUserRequest(CreateUserRequest req) {
        if (req == null) {
            return false;
        }

        if (req.name() == null || req.name().trim().isEmpty()) {
            return false;
        }

        if (req.email() == null || !req.email().toLowerCase().matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
            return false;
        }

        if (req.password() == null || req.password().length() < 12 || req.password().length() > 128) {
            return false;
        }

        return true;
    }

    private boolean validateUpdateUserRequest(UpdateUserRequest req) {
        if (req == null) {
            return false;
        }

        if (req.name() == null || req.name().trim().isEmpty()) {
            return false;
        }

        if (req.email() == null || !req.email().toLowerCase().matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
            return false;
        }

        return true;
    }

    private boolean validateUpdatePasswordRequest(UpdatePasswordRequest req) {
        if (req == null) {
            return false;
        }

        if (req.password() == null || req.password().length() < 12 || req.password().length() > 128) {
            return false;
        }

        return true;
    }

}
