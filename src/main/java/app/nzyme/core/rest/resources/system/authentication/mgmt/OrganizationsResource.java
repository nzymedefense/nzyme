package app.nzyme.core.rest.resources.system.authentication.mgmt;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.requests.*;
import app.nzyme.core.rest.responses.authentication.mgmt.OrganizationDetailsResponse;
import app.nzyme.core.rest.responses.authentication.mgmt.OrganizationsListResponse;
import app.nzyme.core.rest.responses.authentication.mgmt.TenantDetailsResponse;
import app.nzyme.core.rest.responses.authentication.mgmt.TenantsListResponse;
import app.nzyme.core.rest.responses.misc.ErrorResponse;
import app.nzyme.core.security.authentication.PasswordHasher;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

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

        if (nzyme.getAuthenticationService().tenantUserWithEmailExists(
                organizationId, tenantId, req.email().toLowerCase())) {
            LOG.info("Tenant already has a user with same email address.");
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

    private OrganizationDetailsResponse organizationEntryToResponse(OrganizationEntry org) {
        Optional<List<TenantEntry>> tenants = nzyme.getAuthenticationService().findAllTenantsOfOrganization(org.id());
        long organizationTenantCount = tenants.map(List::size).orElse(0);

        return OrganizationDetailsResponse.create(
                org.id(),
                org.name(),
                org.description(),
                org.createdAt(),
                org.updatedAt(),
                organizationTenantCount,
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
                -1,
                nzyme.getAuthenticationService().isTenantDeletable(t)
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

    private boolean validateCreateUserRequest(CreateUserRequest req) {
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

}
