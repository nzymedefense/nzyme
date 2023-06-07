package app.nzyme.core.rest.resources.system.authentication.mgmt;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.events.EventEngineImpl;
import app.nzyme.core.events.actions.EventActionUtilities;
import app.nzyme.core.events.db.EventActionEntry;
import app.nzyme.core.events.types.SystemEvent;
import app.nzyme.core.events.types.SystemEventType;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.*;
import app.nzyme.core.rest.responses.authentication.SessionDetailsResponse;
import app.nzyme.core.rest.responses.authentication.SessionsListResponse;
import app.nzyme.core.rest.responses.authentication.mgmt.*;
import app.nzyme.core.rest.responses.events.EventActionDetailsResponse;
import app.nzyme.core.rest.responses.events.EventActionsListResponse;
import app.nzyme.core.rest.responses.misc.ErrorResponse;
import app.nzyme.core.security.authentication.PasswordHasher;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TapPermissionEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.core.security.authentication.db.UserEntry;
import app.nzyme.core.security.authentication.roles.Permission;
import app.nzyme.core.security.authentication.roles.Permissions;
import app.nzyme.core.security.sessions.db.SessionEntry;
import app.nzyme.core.security.sessions.db.SessionEntryWithUserDetails;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/api/system/authentication/mgmt/organizations")
@Produces(MediaType.APPLICATION_JSON)
public class OrganizationsResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(OrganizationsResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{id}")
    public Response find(@Context SecurityContext sc, @PathParam("id") UUID id) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(id);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(org.get().uuid())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(organizationEntryToResponse(org.get())).build();
    }

    @POST
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
    public Response create(CreateOrganizationRequest req) {
        if (req.name().trim().isEmpty() || req.description().trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        nzyme.getAuthenticationService().createOrganization(req.name(), req.description());

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
    @Path("/show/{id}")
    public Response update(@Context SecurityContext sc,
                           @PathParam("id") UUID id,
                           UpdateOrganizationRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(id);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(org.get().uuid())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().updateOrganization(id, req.name(), req.description());

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants")
    public Response findTenantsOfOrganization(@Context SecurityContext sc,
                                              @PathParam("organizationId") UUID organizationId,
                                              @QueryParam("limit") int limit,
                                              @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(organizationId);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(org.get().uuid())) {
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/administrators")
    public Response findAllOrganizationAdministrators(@Context SecurityContext sc,
                                                      @PathParam("organizationId") UUID organizationId,
                                                      @QueryParam("limit") int limit,
                                                      @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(organizationId);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(org.get().uuid())) {
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/administrators/show/{id}")
    public Response findOrganizationAdministrator(@Context SecurityContext sc,
                                                  @PathParam("organizationId") UUID organizationId,
                                                  @PathParam("id") UUID userId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        Optional<UserEntry> orgAdmin = nzyme.getAuthenticationService().findOrganizationAdministrator(
                organizationId, userId);

        if (orgAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        boolean isDeletable = !authenticatedUser.getUserId().equals(userId);

        return Response.ok(OrganizationAdministratorDetailsResponse.create(
                userEntryToResponse(orgAdmin.get(), Collections.emptyList(), Collections.emptyList()), isDeletable
        )).build();
    }

    @POST
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/administrators")
    public Response createOrganizationAdministrator(@Context SecurityContext sc,
                                                    @PathParam("organizationId") UUID organizationId,
                                                    CreateUserRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!validateCreateUserRequest(req)) {
            LOG.info("Invalid parameters in create user request.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/administrators/show/{id}")
    public Response editOrganizationAdministrator(@Context SecurityContext sc,
                                                  @PathParam("organizationId") UUID organizationId,
                                                  @PathParam("id") UUID userId,
                                                  UpdateUserRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<UserEntry> orgAdmin = nzyme.getAuthenticationService().findOrganizationAdministrator(
                organizationId, userId);

        if (orgAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/administrators/show/{id}/password")
    public Response editOrganizationAdministratorPassword(@Context SecurityContext sc,
                                                          @PathParam("organizationId") UUID organizationId,
                                                          @PathParam("id") UUID userId,
                                                          UpdatePasswordRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<UserEntry> orgAdmin = nzyme.getAuthenticationService().findOrganizationAdministrator(
                organizationId, userId);

        if (orgAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!validateUpdatePasswordRequest(req)) {
            LOG.info("Invalid password in update password request.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        PasswordHasher hasher = new PasswordHasher(nzyme.getMetrics());
        PasswordHasher.GeneratedHashAndSalt hash = hasher.createHash(req.password());

        nzyme.getAuthenticationService().editUserPassword(
                userId,
                hash
        );

        // Invalidate session of user.
        nzyme.getAuthenticationService().deleteAllSessionsOfUser(userId);

        // System event.
        nzyme.getEventEngine().processEvent(SystemEvent.create(
                SystemEventType.AUTHENTICATION_PASSWORD_CHANGED,
                DateTime.now(),
                "Password of organization administrator [" + orgAdmin.get().email() + "] was changed by administrator."
        ), organizationId, null);

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/administrators/show/{id}")
    public Response deleteOrganizationAdministrator(@Context SecurityContext sc,
                                                    @PathParam("organizationId") UUID organizationId,
                                                    @PathParam("id") UUID userId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (authenticatedUser.getUserId() == userId) {
            LOG.warn("Organization administrators cannot delete themselves.");
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/administrators/show/{id}/mfa/reset")
    public Response resetOrganizationAdministratorMFA(@Context SecurityContext sc,
                                                      @PathParam("organizationId") UUID organizationId,
                                                      @PathParam("id") UUID userId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<UserEntry> orgAdmin = nzyme.getAuthenticationService().findOrganizationAdministrator(
                organizationId, userId);

        if (orgAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().resetMFAOfUser(userId);

        LOG.info("Reset MFA credentials of organization administrator [{}] on admin request.",
                orgAdmin.get().email());

        // System event.
        nzyme.getEventEngine().processEvent(SystemEvent.create(
                SystemEventType.AUTHENTICATION_MFA_RESET,
                DateTime.now(),
                "MFA method of organization administrator [" + orgAdmin.get().email() + "] was reset by administrator."
        ), organizationId, null);

        return Response.ok().build();
    }

    @GET
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}")
    public Response findTenantOfOrganization(@Context SecurityContext sc,
                                             @PathParam("organizationId") UUID organizationId,
                                             @PathParam("tenantId") UUID tenantId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<TenantEntry> tenant = nzyme.getAuthenticationService().findTenant(tenantId);

        if (tenant.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(tenantEntryToResponse(tenant.get())).build();
    }

    @POST
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/")
    public Response createTenant(@Context SecurityContext sc,
                                 @PathParam("organizationId") UUID organizationId,
                                 CreateTenantRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (req.name().trim().isEmpty() || req.description().trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().createTenant(organizationId, req.name(), req.description());

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}")
    public Response updateTenant(@Context SecurityContext sc,
                                 @PathParam("organizationId") UUID organizationId,
                                 @PathParam("tenantId") UUID tenantId,
                                 UpdateTenantRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().updateTenant(tenantId, req.name(), req.description());

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}")
    public Response deleteTenant(@Context SecurityContext sc,
                                 @PathParam("organizationId") UUID organizationId,
                                 @PathParam("tenantId") UUID tenantId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().deleteTenant(tenantId);

        return Response.ok().build();
    }

    @GET
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users")
    public Response findAllUsersOfTenant(@Context SecurityContext sc,
                                         @PathParam("organizationId") UUID organizationId,
                                         @PathParam("tenantId") UUID tenantId,
                                         @QueryParam("limit") int limit,
                                         @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}")
    public Response findUserOfTenant(@Context SecurityContext sc,
                                     @PathParam("organizationId") UUID organizationId,
                                     @PathParam("tenantId") UUID tenantId,
                                     @PathParam("userId") UUID userId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Users cannot delete themselves.
        boolean isDeletable = authenticatedUser.getUserId() != userId;

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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users")
    public Response createUserOfTenant(@Context SecurityContext sc,
                                       @PathParam("organizationId") UUID organizationId,
                                       @PathParam("tenantId") UUID tenantId,
                                       CreateUserRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!validateCreateUserRequest(req)) {
            LOG.info("Invalid parameters in create user request.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}")
    public Response editUserOfTenant(@Context SecurityContext sc,
                                     @PathParam("organizationId") UUID organizationId,
                                     @PathParam("tenantId") UUID tenantId,
                                     @PathParam("userId") UUID userId,
                                     UpdateUserRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

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

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}/taps")
    public Response editUserOfTenantTapPermissions(@Context SecurityContext sc,
                                                   @PathParam("organizationId") UUID organizationId,
                                                   @PathParam("tenantId") UUID tenantId,
                                                   @PathParam("userId") UUID userId,
                                                   UpdateUserTapPermissionsRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}/permissions")
    public Response editUserOfTenantPermissions(@Context SecurityContext sc,
                                                @PathParam("organizationId") UUID organizationId,
                                                @PathParam("tenantId") UUID tenantId,
                                                @PathParam("userId") UUID userId,
                                                UpdateUserPermissionsRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().setUserPermissions(user.get().uuid(), req.permissions());

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}")
    public Response deleteUserOfTenant(@Context SecurityContext sc,
                                       @PathParam("organizationId") UUID organizationId,
                                       @PathParam("tenantId") UUID tenantId,
                                       @PathParam("userId") UUID userId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (authenticatedUser.getUserId() == userId) {
            LOG.warn("User [{}] cannot delete themselves.", user.get().email());
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        nzyme.getAuthenticationService().deleteUserOfTenant(organizationId, tenantId, userId);

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}/password")
    public Response editUserOfTenantPassword(@Context SecurityContext sc,
                                             @PathParam("organizationId") UUID organizationId,
                                             @PathParam("tenantId") UUID tenantId,
                                             @PathParam("userId") UUID userId,
                                             UpdatePasswordRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
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

        // System event.
        nzyme.getEventEngine().processEvent(SystemEvent.create(
                SystemEventType.AUTHENTICATION_PASSWORD_CHANGED,
                DateTime.now(),
                "Password of user [" + user.get().email() + "] was changed by administrator."
        ), organizationId, tenantId);

        return Response.ok().build();
    }

    @POST
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/users/show/{userId}/mfa/reset")
    public Response resetMFAOfUserOfTenant(@Context SecurityContext sc,
                                           @PathParam("organizationId") UUID organizationId,
                                           @PathParam("tenantId") UUID tenantId,
                                           @PathParam("userId") UUID userId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserOfTenant(organizationId, tenantId, userId);

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().resetMFAOfUser(user.get().uuid());

        LOG.info("Reset MFA credentials of user [{}] on admin request.", user.get().email());

        // System event.
        nzyme.getEventEngine().processEvent(SystemEvent.create(
                SystemEventType.AUTHENTICATION_MFA_RESET,
                DateTime.now(),
                "MFA method of user [" + user.get().email() + "] was reset by administrator."
        ), organizationId, tenantId);

        return Response.ok().build();
    }

    @GET
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/sessions")
    public Response findSessionsOfOrganization(@Context SecurityContext sc,
                                               @PathParam("organizationId") UUID organizationId,
                                               @QueryParam("limit") int limit,
                                               @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if(!organizationExists(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/sessions")
    public Response findSessionsOfTenant(@Context SecurityContext sc,
                                         @PathParam("organizationId") UUID organizationId,
                                         @PathParam("tenantId") UUID tenantId,
                                         @QueryParam("limit") int limit,
                                         @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/sessions/show/{sessionId}")
    public Response invalidateSession(@Context SecurityContext sc,
                                      @PathParam("sessionId") long sessionId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<SessionEntry> session = nzyme.getAuthenticationService()
                .findSessionWithOrWithoutPassedMFAById(sessionId);

        if (session.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<UserEntry> user = nzyme.getAuthenticationService().findUserById(session.get().userId());

        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(user.get().organizationId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().deleteSession(sessionId);

        return Response.ok().build();
    }

    @GET
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/taps/show/{tapUuid}")
    public Response findTap(@Context SecurityContext sc,
                            @PathParam("organizationId") UUID organizationId,
                            @PathParam("tenantId") UUID tenantId,
                            @PathParam("tapUuid") UUID tapId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<TapPermissionEntry> tap = nzyme.getAuthenticationService().findTap(organizationId, tenantId, tapId);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(tapPermissionEntryToResponse(tap.get())).build();
    }

    @POST
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/taps")
    public Response createTap(@Context SecurityContext sc,
                              @PathParam("organizationId") UUID organizationId,
                              @PathParam("tenantId") UUID tenantId,
                              CreateTapRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (req.name().trim().isEmpty() || req.description().trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/taps/show/{tapUuid}")
    public Response editTap(@Context SecurityContext sc,
                            @PathParam("organizationId") UUID organizationId,
                            @PathParam("tenantId") UUID tenantId,
                            @PathParam("tapUuid") UUID tapId,
                            UpdateTapRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<TapPermissionEntry> tap = nzyme.getAuthenticationService().findTap(organizationId, tenantId, tapId);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().editTap(organizationId, tenantId, tapId, req.name(), req.description());

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/taps/show/{tapUuid}")
    public Response deleteTap(@Context SecurityContext sc,
                              @PathParam("organizationId") UUID organizationId,
                              @PathParam("tenantId") UUID tenantId,
                              @PathParam("tapUuid") UUID tapId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<TapPermissionEntry> tap = nzyme.getAuthenticationService().findTap(organizationId, tenantId, tapId);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().deleteTap(organizationId, tenantId, tapId);

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/tenants/show/{tenantId}/taps/show/{tapUuid}/secret/cycle")
    public Response cycleTapSecret(@Context SecurityContext sc,
                                   @PathParam("organizationId") UUID organizationId,
                                   @PathParam("tenantId") UUID tenantId,
                                   @PathParam("tapUuid") UUID tapId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationAndTenantExists(organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if user is org admin for this org.
        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
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
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/events/actions")
    public Response findAllEventActionsOfOrganization(@Context SecurityContext sc,
                                                      @PathParam("organizationId") UUID organizationId,
                                                      @QueryParam("limit") int limit,
                                                      @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationExists(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        EventEngineImpl eventEngine = (EventEngineImpl) nzyme.getEventEngine();

        long total = eventEngine.countAllEventActionsOfOrganization(organizationId);
        List<EventActionDetailsResponse> events = Lists.newArrayList();
        for (EventActionEntry ea : eventEngine.findAllEventActionsOfOrganization(organizationId, limit, offset)) {
            List<SystemEventType> subs = eventEngine.findAllEventTypesActionIsSubscribedTo(ea.uuid());
            events.add(EventActionUtilities.eventActionEntryToResponse(ea, subs));
        }

        return Response.ok(EventActionsListResponse.create(total, events)).build();
    }

    @GET
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{organizationId}/events/actions/show/{actionId}")
    public Response findEventActionOfOrganization(@Context SecurityContext sc,
                                                  @PathParam("organizationId") UUID organizationId,
                                                  @PathParam("actionId") UUID actionId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!organizationExists(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!authenticatedUser.isSuperAdministrator() && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        EventEngineImpl eventEngine = (EventEngineImpl) nzyme.getEventEngine();
        Optional<EventActionEntry> ea = eventEngine.findEventActionOfOrganization(organizationId, actionId);

        if (ea.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<SystemEventType> subs = eventEngine.findAllEventTypesActionIsSubscribedTo(ea.get().uuid());

        return Response.ok(EventActionUtilities.eventActionEntryToResponse(ea.get(), subs)).build();
    }

    @GET
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
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
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
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
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
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
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
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
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
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

        // System event.
        nzyme.getEventEngine().processEvent(SystemEvent.create(
                SystemEventType.AUTHENTICATION_PASSWORD_CHANGED,
                DateTime.now(),
                "Password of super administrator [" + superAdmin.get().email() + "] was changed by administrator."
        ), null, null);

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
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
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
    @Path("/superadmins/show/{id}/mfa/reset")
    public Response resetSuperAdministratorMFA(@PathParam("id") UUID userId) {
        Optional<UserEntry> superAdmin = nzyme.getAuthenticationService().findSuperAdministrator(userId);

        if (superAdmin.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getAuthenticationService().resetMFAOfUser(superAdmin.get().uuid());

        LOG.info("Reset MFA credentials of super administrator [{}] on admin request.", superAdmin.get().email());

        // System event.
        nzyme.getEventEngine().processEvent(SystemEvent.create(
                SystemEventType.AUTHENTICATION_MFA_RESET,
                DateTime.now(),
                "MFA method of super administrator [" + superAdmin.get().email() + "] was reset by administrator."
        ), null, null);

        return Response.ok().build();
    }

    @GET
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
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
                tapPermissions,
                u.isLoginThrottled()
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
