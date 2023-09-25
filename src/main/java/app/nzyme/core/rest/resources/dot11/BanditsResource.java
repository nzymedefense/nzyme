package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.monitoring.CustomBanditDescription;
import app.nzyme.core.dot11.monitoring.Dot11BanditDescription;
import app.nzyme.core.dot11.monitoring.Dot11Bandits;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
<<<<<<< Updated upstream
import app.nzyme.core.rest.requests.CreateBanditFingerprintRequest;
import app.nzyme.core.rest.requests.CreateCustomBanditRequest;
import app.nzyme.core.rest.requests.UpdateCustomBanditRequest;
=======
import app.nzyme.core.rest.requests.CreateCustomBanditRequest;
>>>>>>> Stashed changes
import app.nzyme.core.rest.responses.dot11.monitoring.BuiltinBanditDetailsResponse;
import app.nzyme.core.rest.responses.dot11.monitoring.CustomBanditDetailsResponse;
import app.nzyme.core.rest.responses.dot11.monitoring.CustomBanditListResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/dot11/bandits")
@Produces(MediaType.APPLICATION_JSON)
public class BanditsResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(BanditsResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/builtin")
    public Response findAllBuiltIn() {
        List<BuiltinBanditDetailsResponse> bandits = Lists.newArrayList();

        for (Dot11BanditDescription bandit : Dot11Bandits.BUILT_IN) {
            bandits.add(BuiltinBanditDetailsResponse.create(
                    bandit.id(),
                    bandit.name(),
                    bandit.description(),
                    bandit.fingerprints() == null ? Collections.emptyList() : bandit.fingerprints()
            ));
        }

        return Response.ok(bandits).build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/builtin/show/{id}")
    public Response findOneBuiltIn(@PathParam("id") @NotEmpty String id) {
        Dot11BanditDescription bandit = null;
        for (Dot11BanditDescription b : Dot11Bandits.BUILT_IN) {
            if (b.id().equals(id)) {
                bandit = b;
                break;
            }
        }

        if (bandit == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(BuiltinBanditDetailsResponse.create(
                bandit.id(),
                bandit.name(),
                bandit.description(),
                bandit.fingerprints() == null ? Collections.emptyList() : bandit.fingerprints()
        )).build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/custom")
    public Response findAllCustom(@Context SecurityContext sc,
                                  @QueryParam("limit") int limit,
                                  @QueryParam("offset") int offset,
                                  @QueryParam("organization_uuid") @NotNull UUID organizationId,
                                  @QueryParam("tenant_uuid") @NotNull UUID tenantId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!hasPermissions(authenticatedUser, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long total = nzyme.getDot11().countCustomBandits(organizationId, tenantId);
        List<CustomBanditDetailsResponse> bandits = Lists.newArrayList();

        for (CustomBanditDescription b : nzyme.getDot11().findAllCustomBandits(organizationId, tenantId, limit, offset)) {
            // Find fingerprints of bandit.
            List<String> fingerprints = nzyme.getDot11().findFingerprintsOfCustomBandit(b.id());

            bandits.add(CustomBanditDetailsResponse.create(
                    b.uuid(),
                    b.name(),
                    b.description(),
                    fingerprints,
                    b.createdAt(),
                    b.updatedAt()
            ));
        }

        return Response.ok(CustomBanditListResponse.create(total, bandits)).build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/custom/show/{id}")
    public Response findCustom(@Context SecurityContext sc,
                               @PathParam("id") @NotNull UUID id) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<CustomBanditDescription> bandit = nzyme.getDot11().findCustomBandit(id);

        if (bandit.isEmpty()
                || !hasPermissions(authenticatedUser, bandit.get().organizationId(), bandit.get().tenantId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<String> fingerprints = nzyme.getDot11().findFingerprintsOfCustomBandit(bandit.get().id());

        return Response.ok(CustomBanditDetailsResponse.create(
                bandit.get().uuid(),
                bandit.get().name(),
                bandit.get().description(),
<<<<<<< Updated upstream
                fingerprints,
                bandit.get().createdAt(),
                bandit.get().updatedAt()
=======
                fingerprints
>>>>>>> Stashed changes
        )).build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/custom")
    public Response createCustom(@Context SecurityContext sc, @Valid CreateCustomBanditRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!hasPermissions(authenticatedUser, req.organizationId(), req.tenantId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().createCustomBandit(req.organizationId(), req.tenantId(), req.name(), req.description());

        return Response.status(Response.Status.CREATED).build();
    }

<<<<<<< Updated upstream
    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/custom/show/{id}")
    public Response editCustom(@Context SecurityContext sc,
                               @PathParam("id") @NotNull UUID id,
                               @Valid UpdateCustomBanditRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<CustomBanditDescription> bandit = nzyme.getDot11().findCustomBandit(id);

        if (bandit.isEmpty()
                || !hasPermissions(authenticatedUser, bandit.get().organizationId(), bandit.get().tenantId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().editCustomBandit(bandit.get().id(), req.name(), req.description());
        nzyme.getDot11().bumpCustomBanditUpdatedAt(bandit.get().id());

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/custom/show/{id}")
    public Response deleteCustom(@Context SecurityContext sc, @PathParam("id") @NotNull UUID id) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<CustomBanditDescription> bandit = nzyme.getDot11().findCustomBandit(id);

        if (bandit.isEmpty()
                || !hasPermissions(authenticatedUser, bandit.get().organizationId(), bandit.get().tenantId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteCustomBandit(bandit.get().id());

        return Response.ok().build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/custom/show/{id}/fingerprints")
    public Response addFingerprint(@Context SecurityContext sc,
                                   @PathParam("id") @NotNull UUID id,
                                   @Valid CreateBanditFingerprintRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<CustomBanditDescription> bandit = nzyme.getDot11().findCustomBandit(id);

        if (bandit.isEmpty()
                || !hasPermissions(authenticatedUser, bandit.get().organizationId(), bandit.get().tenantId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (req.fingerprint().length() != 64) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        List<String> fingerprints = nzyme.getDot11().findFingerprintsOfCustomBandit(bandit.get().id());

        // Check if fingerprint already exists.
        if (fingerprints.contains(req.fingerprint())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        nzyme.getDot11().addFingerprintOfCustomBandit(bandit.get().id(), req.fingerprint());
        nzyme.getDot11().bumpCustomBanditUpdatedAt(bandit.get().id());

        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/custom/show/{id}/fingerprints/show/{fingerprint}")
    public Response removeFingerprint(@Context SecurityContext sc,
                                      @PathParam("id") @NotNull UUID id,
                                      @PathParam("fingerprint") @NotEmpty String fingerprint) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<CustomBanditDescription> bandit = nzyme.getDot11().findCustomBandit(id);

        if (bandit.isEmpty()
                || !hasPermissions(authenticatedUser, bandit.get().organizationId(), bandit.get().tenantId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().removeFingerprintOfCustomBandit(bandit.get().id(), fingerprint);
        nzyme.getDot11().bumpCustomBanditUpdatedAt(bandit.get().id());

        return Response.ok().build();
    }

=======
>>>>>>> Stashed changes
    private boolean hasPermissions(AuthenticatedUser authenticatedUser, UUID organizationId, UUID tenantId) {
        if (!authenticatedUser.isSuperAdministrator()) {
            if (authenticatedUser.isOrganizationAdministrator()) {
                // Org admin. Must be their org.
                if (!organizationId.equals(authenticatedUser.getOrganizationId())) {
                    return false;
                }
            } else {
                // Tenant user. Must be their org and tenant.
                if (!organizationId.equals(authenticatedUser.getOrganizationId())
                        || !tenantId.equals(authenticatedUser.getTenantId())) {
                    return false;
                }
            }
        }

        return true;
    }

}
