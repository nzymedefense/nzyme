package app.nzyme.core.rest.resources.system.authentication.mgmt;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
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
import app.nzyme.core.security.authentication.roles.Permission;
import app.nzyme.core.security.authentication.roles.Permissions;
import app.nzyme.core.security.sessions.db.SessionEntryWithUserDetails;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/system/authentication/mgmt/organizations")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class OrganizationsResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(OrganizationsResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response findAll(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<OrganizationDetailsResponse> organizations = Lists.newArrayList();

        for (OrganizationEntry org : nzyme.getAuthenticationService().findAllOrganizations(limit, offset)) {
            organizations.add(organizationEntryToResponse(org));
        }

        long organizationCount = nzyme.getAuthenticationService().countAllOrganizations();

        return Response.ok(OrganizationsListResponse.create(organizationCount, organizations)).build();
    }

    @GET
    @Path("/show/{id}")
    public Response find(@PathParam("id") UUID id) {
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
    public Response update(@PathParam("id") UUID id, UpdateOrganizationRequest req) {
        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(id);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().updateOrganization(id, req.name(), req.description());

        return Response.ok().build();
    }

    @DELETE
    @Path("/show/{id}")
    public Response delete(@PathParam("id") UUID id) {
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
    public Response findTenantsOfOrganization(@PathParam("organizationId") UUID organizationId,
                                              @QueryParam("limit") int limit,
                                              @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(organizationId);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<List<TenantEntry>> tenants = nzyme.getAuthenticationService().findAllTenantsOfOrganization(
                org.get().uuid(), limit, offset);

        List<TenantDetailsResponse> response = Lists.newArrayList();
        if (tenants.isPresent()) {
            for (TenantEntry tenant : tenants.get()) {
                response.add(tenantEntryToResponse(tenant));
            }
        }

        long tenantsCount = nzyme.getAuthenticationService().countTenantsOfOrganization(org.get());

        return Response.ok(TenantsListResponse.create(tenantsCount, response)).build();
    }


    @GET
    @Path("/show/{organizationId}/administrators")
    public Response findAllOrganizationAdministrators(@PathParam("organizationId") UUID organizationId,
                                                      @QueryParam("limit") int limit,
                                                      @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(organizationId);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<UserDetailsResponse> users = Lists.newArrayList();
        for (UserEntry user : nzyme.getAuthenticationService().findAllOrganizationAdministrators(
                org.get().uuid(), limit, offset)) {
            users.add(userEntryToResponse(user, Collections.emptyList(), Collections.emptyList()));
        }

        long orgAdminCount = nzyme.getAuthenticationService().countOrganizationAdministrators(org.get().uuid());

        return Response.ok(UsersListResponse.create(orgAdminCount, users)).build();
    }

    @GET
    @Path("/show/{organizationId}/administrators/show/{id}")
    public Response findOrganizationAdministrator(@Context SecurityContext sc,
                                                  @PathParam("organizationId") UUID organizationId,
                                                  @PathParam("id") UUID userId) {
        AuthenticatedUser sessionUser = getAuthenticatedUser(sc);
        Optional<UserEntry> orgAdmin = nzyme.getAuthenticationService().findOrganizationAdministrator(
                organizationId, userId);

        if (orgAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        boolean isDeletable = !sessionUser.getUserId().equals(userId);

        return Response.ok(OrganizationAdministratorDetailsResponse.create(
                userEntryToResponse(orgAdmin.get(), Collections.emptyList(), Collections.emptyList()), isDeletable
        )).build();
    }

    @POST
    @Path("/show/{organizationId}/administrators")
    public Response createOrganizationAdministrator(@PathParam("organizationId") UUID organizationId,
                                                    CreateUserRequest req) {
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

        nzyme.getAuthenticationService().createOrganizationAdministrator(
                organizationId,
                req.name(),
                req.email().toLowerCase(),
                hash
        );

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/show/{organizationId}/administrators/show/{id}")
    public Response editOrganizationAdministrator(@PathParam("organizationId") UUID organizationId,
                                                  @PathParam("id") UUID userId,
                                                  UpdateUserRequest req) {
        Optional<UserEntry> orgAdmin = nzyme.getAuthenticationService().findOrganizationAdministrator(
                organizationId, userId);

        if (orgAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!validateUpdateUserRequest(req)) {
            LOG.info("Invalid parameters in update user request.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!orgAdmin.get().email().equals(req.email()) && nzyme.getAuthenticationService().userWithEmailExists(
                req.email().toLowerCase())) {
            LOG.info("User with email address already exists.");
            return Response.status(Response.Status.UNAUTHORIZED).entity(
                    ErrorResponse.create("Email address already in use.")
            ).build();
        }

        nzyme.getAuthenticationService().editUser(
                userId,
                req.name(),
                req.email().toLowerCase()
        );

        return Response.ok().build();
    }

    @PUT
    @Path("/show/{organizationId}/administrators/show/{id}/password")
    public Response editOrganizationAdministratorPassword(@PathParam("organizationId") UUID organizationId,
                                                          @PathParam("id") UUID userId,
                                                          UpdatePasswordRequest req) {
        Optional<UserEntry> orgAdmin = nzyme.getAuthenticationService().findOrganizationAdministrator(
                organizationId, userId);

        if (orgAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!validateUpdatePasswordRequest(req)) {
            LOG.info("Invalid password in update password request.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        PasswordHasher hasher = new PasswordHasher(nzyme.getMetrics());
        PasswordHasher.GeneratedHashAndSalt hash = hasher.createHash(req.password());

        nzyme.getAuthenticationService().editUserPassword(
                userId,
                hash
        );

        // Invalidate session of user.
        nzyme.getAuthenticationService().deleteAllSessionsOfUser(userId);

        return Response.ok().build();
    }


    @DELETE
    @Path("/show/{organizationId}/administrators/show/{id}")
    public Response deleteOrganizationAdministrator(@Context SecurityContext sc,
                                                    @PathParam("organizationId") UUID organizationId,
                                                    @PathParam("id") UUID userId) {
        AuthenticatedUser sessionUser = getAuthenticatedUser(sc);

        if (sessionUser.getUserId() == userId) {
            LOG.warn("Organization administrators cannot delete themselves.");
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        Optional<UserEntry> orgAdmin = nzyme.getAuthenticationService().findOrganizationAdministrator(
                organizationId, userId);

        if (orgAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().deleteOrganizationAdministrator(organizationId, userId);

        return Response.ok().build();
    }

    @POST
    @Path("/show/{organizationId}/administrators/show/{id}/mfa/reset")
    public Response resetOrganizationAdministratorMFA(@PathParam("organizationId") UUID organizationId,
                                                      @PathParam("id") UUID userId) {
        Optional<UserEntry> orgAdmin = nzyme.getAuthenticationService().findOrganizationAdministrator(
                organizationId, userId);

        if (orgAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().resetMFAOfUser(userId);

        LOG.info("Reset MFA credentials of organization administrator [{}] on admin request.",
                orgAdmin.get().email());

        return Response.ok().build();
    }

    @GET
    @Path("/show/{organizationId}/tenants/show/{tenantId}")
    public Response findTenantOfOrganization(@PathParam("organizationId") UUID organizationId,
                                             @PathParam("tenantId") UUID tenantId) {
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
    public Response createTenant(@PathParam("organizationId") UUID organizationId, CreateTenantRequest req) {
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
    public Response updateTenant(@PathParam("organizationId") UUID organizationId,
                                 @PathParam("tenantId") UUID tenantId,
                                 UpdateTenantRequest req) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().updateTenant(tenantId, req.name(), req.description());

        return Response.ok().build();
    }

    @DELETE
    @Path("/show/{organizationId}/tenants/show/{tenantId}")
    public Response deleteTenant(@PathParam("organizationId") UUID organizationId,
                                 @PathParam("tenantId") UUID tenantId) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().deleteTenant(tenantId);

        return Response.ok().build();
    }

    @GET
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users")
    public Response findAllUsersOfTenant(@PathParam("organizationId") UUID organizationId,
                                         @PathParam("tenantId") UUID tenantId,
                                         @QueryParam("limit") int limit,
                                         @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<UserDetailsResponse> users = Lists.newArrayList();
        for (UserEntry user : nzyme.getAuthenticationService().findAllUsersOfTenant(
                organizationId, tenantId, limit, offset)) {
            users.add(userEntryToResponse(
                    user,
                    nzyme.getAuthenticationService().findPermissionsOfUser(user.uuid()),
                    nzyme.getAuthenticationService().findTapPermissionsOfUser(user.uuid())
            ));
        }

        long userCount = nzyme.getAuthenticationService().countUsersOfTenant(
                nzyme.getAuthenticationService().findTenant(tenantId).get()
        );

        return Response.ok(UsersListResponse.create(userCount, users)).build();
    }

    @GET
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}")
    public Response findUserOfTenant(@Context SecurityContext sc,
                                     @PathParam("organizationId") UUID organizationId,
                                     @PathParam("tenantId") UUID tenantId,
                                     @PathParam("userId") UUID userId) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Users cannot delete themselves.
        AuthenticatedUser sessionUser = getAuthenticatedUser(sc);
        boolean isDeletable = sessionUser.getUserId() != userId;

        return Response.ok(UserOfTenantDetailsResponse.create(
                userEntryToResponse(
                        user.get(),
                        nzyme.getAuthenticationService().findPermissionsOfUser(user.get().uuid()),
                        nzyme.getAuthenticationService().findTapPermissionsOfUser(user.get().uuid())
                ),
                isDeletable)
        ).build();
    }

    @POST
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users")
    public Response createUserOfTenant(@PathParam("organizationId") UUID organizationId,
                                       @PathParam("tenantId") UUID tenantId,
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
    public Response editUserOfTenant(@PathParam("organizationId") UUID organizationId,
                                     @PathParam("tenantId") UUID tenantId,
                                     @PathParam("userId") UUID userId,
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

        nzyme.getAuthenticationService().editUser(
                userId,
                req.name(),
                req.email().toLowerCase()
        );

        return Response.ok().build();
    }

    @PUT
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}/taps")
    public Response editUserOfTenantTapPermissions(@PathParam("organizationId") UUID organizationId,
                                                   @PathParam("tenantId") UUID tenantId,
                                                   @PathParam("userId") UUID userId,
                                                   UpdateUserTapPermissionsRequest req) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Update tap permissions.
        List<UUID> requestedPermissions = Lists.newArrayList();

        for (String tap : req.taps()) {
            requestedPermissions.add(UUID.fromString(tap));
        }

        List<UUID> newPermissions = Lists.newArrayList();

        // Make sure tap belongs to tenant and exists.
        for (TapPermissionEntry tap : nzyme.getAuthenticationService().findAllTapsOfTenant(organizationId, tenantId)) {
            if (requestedPermissions.contains(tap.uuid())) {
                newPermissions.add(tap.uuid());
            }
        }

        nzyme.getAuthenticationService().setUserTapPermissions(user.get().uuid(), newPermissions);

        // Update access all flag.
        nzyme.getAuthenticationService().setUserTapPermissionsAllowAll(user.get().uuid(), req.allowAccessAllTenantTaps());

        return Response.ok().build();
    }

    @PUT
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}/permissions")
    public Response editUserOfTenantPermissions(@PathParam("organizationId") UUID organizationId,
                                                @PathParam("tenantId") UUID tenantId,
                                                @PathParam("userId") UUID userId,
                                                UpdateUserPermissionsRequest req) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().setUserPermissions(user.get().uuid(), req.permissions());

        return Response.ok().build();
    }

    @DELETE
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}")
    public Response deleteUserOfTenant(@Context SecurityContext sc,
                                       @PathParam("organizationId") UUID organizationId,
                                       @PathParam("tenantId") UUID tenantId,
                                       @PathParam("userId") UUID userId) {
        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        AuthenticatedUser sessionUser = getAuthenticatedUser(sc);
        if (sessionUser.getUserId() == userId) {
            LOG.warn("User [{}] cannot delete themselves.", user.get().email());
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        nzyme.getAuthenticationService().deleteUserOfTenant(organizationId, tenantId, userId);

        return Response.ok().build();
    }

    @PUT
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}/password")
    public Response editUserOfTenantPassword(@PathParam("organizationId") UUID organizationId,
                                             @PathParam("tenantId") UUID tenantId,
                                             @PathParam("userId") UUID userId,
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

        nzyme.getAuthenticationService().editUserPassword(
                userId,
                hash
        );

        // Invalidate session of user.
        nzyme.getAuthenticationService().deleteAllSessionsOfUser(user.get().uuid());

        return Response.ok().build();
    }

    @POST
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}/mfa/reset")
    public Response resetMFAOfUserOfTenant(@PathParam("organizationId") UUID organizationId,
                                           @PathParam("tenantId") UUID tenantId,
                                           @PathParam("userId") UUID userId) {
        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().resetMFAOfUser(user.get().uuid());

        LOG.info("Reset MFA credentials of user [{}] on admin request.", user.get().email());

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
                    session.lastActivity(),
                    session.mfaValid(),
                    session.mfaRequestedAt()
            ));
        }

        long sessionCount = nzyme.getAuthenticationService().countAllSessions();

        return Response.ok(SessionsListResponse.create(sessionCount, sessions)).build();
    }

    @GET
    @Path("/show/{organizationId}/sessions")
    public Response findSessionsOfOrganization(@PathParam("organizationId") UUID organizationId,
                                               @QueryParam("limit") int limit,
                                               @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
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
                    session.lastActivity(),
                    session.mfaValid(),
                    session.mfaRequestedAt()
            ));
        }

        long sessionCount = nzyme.getAuthenticationService().countSessionsOfOrganization(organizationId);

        return Response.ok(SessionsListResponse.create(sessionCount, sessions)).build();
    }

    @GET
    @Path("/show/{organizationId}/tenants/show/{tenantId}/sessions")
    public Response findSessionsOfTenant(@PathParam("organizationId") UUID organizationId,
                                         @PathParam("tenantId") UUID tenantId,
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
                    session.lastActivity(),
                    session.mfaValid(),
                    session.mfaRequestedAt()
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
    public Response findAllTaps(@PathParam("organizationId") UUID organizationId,
                                @PathParam("tenantId") UUID tenantId,
                                @QueryParam("limit") int limit,
                                @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<TapPermissionDetailsResponse> taps = Lists.newArrayList();
        for (TapPermissionEntry tap : nzyme.getAuthenticationService()
                .findAllTapsOfTenant(organizationId, tenantId, limit, offset)) {
            taps.add(tapPermissionEntryToResponse(tap));
        }

        long tapCount = nzyme.getAuthenticationService().countTapsOfTenant(
                nzyme.getAuthenticationService().findTenant(tenantId).get()
        );

        return Response.ok(TapPermissionsListResponse.create(tapCount, taps)).build();
    }

    @GET
    @Path("/show/{organizationId}/tenants/show/{tenantId}/taps/show/{tapUuid}")
    public Response findTap(@PathParam("organizationId") UUID organizationId,
                            @PathParam("tenantId") UUID tenantId,
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
    public Response createTap(@PathParam("organizationId") UUID organizationId,
                              @PathParam("tenantId") UUID tenantId,
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
    public Response editTap(@PathParam("organizationId") UUID organizationId,
                            @PathParam("tenantId") UUID tenantId,
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
    public Response deleteTap(@PathParam("organizationId") UUID organizationId,
                            @PathParam("tenantId") UUID tenantId,
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
    public Response cycleTapSecret(@PathParam("organizationId") UUID organizationId,
                                   @PathParam("tenantId") UUID tenantId,
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

    @GET
    @Path("/superadmins")
    public Response findAllSuperAdministrators(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<UserDetailsResponse> users = Lists.newArrayList();
        for (UserEntry user : nzyme.getAuthenticationService().findAllSuperAdministrators(limit, offset)) {
            users.add(userEntryToResponse(
                    user,
                    Collections.emptyList(),
                    Collections.emptyList())
            );
        }

        long superadminCount = nzyme.getAuthenticationService().countSuperAdministrators();

        return Response.ok(UsersListResponse.create(superadminCount, users)).build();
    }

    @GET
    @Path("/superadmins/show/{id}")
    public Response findSuperAdministrator(@Context SecurityContext sc, @PathParam("id") UUID userId) {
        AuthenticatedUser sessionUser = getAuthenticatedUser(sc);
        Optional<UserEntry> superAdmin = nzyme.getAuthenticationService().findSuperAdministrator(userId);

        if (superAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        boolean isDeletable = nzyme.getAuthenticationService().countSuperAdministrators() != 1
                && sessionUser.getUserId() != userId;

        return Response.ok(SuperAdministratorDetailsResponse.create(
                userEntryToResponse(
                        superAdmin.get(),
                        Collections.emptyList(),
                        Collections.emptyList()
                ), isDeletable
        )).build();
    }

    @POST
    @Path("/superadmins")
    public Response createSuperAdministrator(CreateUserRequest req) {
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

        nzyme.getAuthenticationService().createSuperAdministrator(
                req.name(),
                req.email().toLowerCase(),
                hash
        );

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/superadmins/show/{userId}")
    public Response editSuperAdministrator(@PathParam("userId") UUID userId,
                                           UpdateUserRequest req) {
        Optional<UserEntry> superAdmin = nzyme.getAuthenticationService().findSuperAdministrator(userId);

        if (superAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!validateUpdateUserRequest(req)) {
            LOG.info("Invalid parameters in update user request.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!superAdmin.get().email().equals(req.email()) && nzyme.getAuthenticationService().userWithEmailExists(
                req.email().toLowerCase())) {
            LOG.info("User with email address already exists.");
            return Response.status(Response.Status.UNAUTHORIZED).entity(
                    ErrorResponse.create("Email address already in use.")
            ).build();
        }

        nzyme.getAuthenticationService().editUser(
                userId,
                req.name(),
                req.email().toLowerCase()
        );

        return Response.ok().build();
    }

    @PUT
    @Path("/superadmins/show/{userId}/password")
    public Response editSuperAdministratorPassword(@PathParam("userId") UUID userId, UpdatePasswordRequest req) {
        Optional<UserEntry> superAdmin = nzyme.getAuthenticationService().findSuperAdministrator(userId);

        if (superAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!validateUpdatePasswordRequest(req)) {
            LOG.info("Invalid password in update password request.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        PasswordHasher hasher = new PasswordHasher(nzyme.getMetrics());
        PasswordHasher.GeneratedHashAndSalt hash = hasher.createHash(req.password());

        nzyme.getAuthenticationService().editUserPassword(
                userId,
                hash
        );

        // Invalidate session of user.
        nzyme.getAuthenticationService().deleteAllSessionsOfUser(superAdmin.get().uuid());

        return Response.ok().build();
    }


    @DELETE
    @Path("/superadmins/show/{id}")
    public Response deleteSuperAdministrator(@Context SecurityContext sc, @PathParam("id") UUID userId) {
        AuthenticatedUser sessionUser = getAuthenticatedUser(sc);

        if (sessionUser.getUserId().equals(userId)) {
            LOG.warn("Super administrators cannot delete themselves.");
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        if (nzyme.getAuthenticationService().countSuperAdministrators() == 1) {
            LOG.warn("Last remaining super administrator cannot be deleted.");
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        Optional<UserEntry> superAdmin = nzyme.getAuthenticationService().findSuperAdministrator(userId);

        if (superAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().deleteSuperAdministrator(userId);

        return Response.ok().build();
    }

    @POST
    @Path("/superadmins/show/{id}/mfa/reset")
    public Response resetSuperAdministratorMFA(@PathParam("id") UUID userId) {
        Optional<UserEntry> superAdmin = nzyme.getAuthenticationService().findSuperAdministrator(userId);

        if (superAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().resetMFAOfUser(superAdmin.get().uuid());

        LOG.info("Reset MFA credentials of super administrator [{}] on admin request.", superAdmin.get().email());

        return Response.ok().build();
    }

    @GET
    @Path("/permissions/all")
    public Response getAllPermissions() {
        List<PermissionDetailsResponse> permissions = Lists.newArrayList();

        for (Permission permission : Permissions.ALL.values()) {
            permissions.add(PermissionDetailsResponse.create(
                    permission.id(),
                    permission.name(),
                    permission.description(),
                    permission.respectsTapScope()
            ));
        }

        return Response.ok(PermissionListResponse.create(permissions)).build();
    }

    private OrganizationDetailsResponse organizationEntryToResponse(OrganizationEntry org) {
        return OrganizationDetailsResponse.create(
                org.uuid(),
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
                t.uuid(),
                t.organizationUuid(),
                t.name(),
                t.description(),
                t.createdAt(),
                t.updatedAt(),
                nzyme.getAuthenticationService().countUsersOfTenant(t),
                nzyme.getAuthenticationService().countTapsOfTenant(t),
                nzyme.getAuthenticationService().isTenantDeletable(t)
        );
    }

    private UserDetailsResponse userEntryToResponse(UserEntry u, List<String> permissions, List<UUID> tapPermissions) {
        return UserDetailsResponse.create(
                u.uuid(),
                u.organizationId(),
                u.tenantId(),
                u.email(),
                u.name(),
                u.createdAt(),
                u.updatedAt(),
                u.lastActivity(),
                u.lastRemoteIp(),
                u.lastGeoCity(),
                u.lastGeoCountry(),
                u.lastGeoAsn(),
                permissions,
                u.accessAllTenantTaps(),
                tapPermissions
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

    private boolean organizationExists(UUID organizationId) {
        return nzyme.getAuthenticationService().findOrganization(organizationId).isPresent();
    }

    private boolean organizationAndTenantExists(UUID organizationId, UUID tenantId) {
        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(organizationId);

        if (org.isEmpty()) {
            return false;
        }

        Optional<TenantEntry> tenant = nzyme.getAuthenticationService().findTenant(tenantId);

        return tenant.isPresent();
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
