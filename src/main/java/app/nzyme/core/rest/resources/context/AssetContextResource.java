package app.nzyme.core.rest.resources.context;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.Subsystem;
import app.nzyme.core.context.db.MacAddressContextEntry;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.requests.CreateMacAddressContextRequest;
import app.nzyme.core.rest.responses.context.MacAddressContextDetailsResponse;
import app.nzyme.core.rest.responses.context.MacAddressContextListResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.UUID;

@Path("/api/context")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class AssetContextResource extends UserAuthenticatedResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "mac_aliases_manage" })
    @Path("/mac/organization/show/{organization_id}/tenant/show/{tenant_id}")
    public Response macs(@Context SecurityContext sc,
                         @PathParam("organization_id") UUID organizationId,
                         @PathParam("tenant_id") UUID tenantId,
                         @QueryParam("limit") @Max(250) int limit,
                         @QueryParam("offset") int offset,
                         @QueryParam("subsystem") @Nullable String subsystem) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long count = nzyme.getContextService().countMacAddressContext(organizationId, tenantId);

        List<MacAddressContextDetailsResponse> addresses = Lists.newArrayList();
        for (MacAddressContextEntry m : nzyme.getContextService()
                .findAllMacAddressContext(organizationId, tenantId, limit, offset)) {
            addresses.add(MacAddressContextDetailsResponse.create(
                    m.uuid(),
                    m.macAddress(),
                    m.name(),
                    m.description(),
                    m.notes(),
                    m.createdAt(),
                    m.updatedAt()
            ));
        }


        return Response.ok(MacAddressContextListResponse.create(count, addresses)).build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "mac_aliases_manage" })
    @Path("/mac")
    public Response macs(@Context SecurityContext sc, CreateMacAddressContextRequest req) {
        if (!passedTenantDataAccessible(sc, req.organizationId(), req.tenantId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getContextService().createMacAddressContext(
                req.macAddress(),
                req.name(),
                req.description(),
                req.notes(),
                req.organizationId(),
                req.tenantId()
        );

        return Response.status(Response.Status.CREATED).build();
    }


}
