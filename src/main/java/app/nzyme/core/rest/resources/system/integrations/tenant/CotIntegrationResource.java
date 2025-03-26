package app.nzyme.core.rest.resources.system.integrations.tenant;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.integrations.tenant.cot.db.CotOutputEntry;
import app.nzyme.core.quota.QuotaType;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.requests.CreateCotOutputRequest;
import app.nzyme.core.rest.responses.integrations.tenant.cot.CotOutputDetailsResponse;
import app.nzyme.core.rest.responses.integrations.tenant.cot.CotOutputListResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.UUID;

@Path("/api/system/authentication/mgmt/organizations/show/{organizationId}/tenants/show/{tenantId}/integrations/cot")
@RESTSecured(PermissionLevel.ORGADMINISTRATOR)
@Produces(MediaType.APPLICATION_JSON)
public class CotIntegrationResource extends UserAuthenticatedResource  {

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response all(@Context SecurityContext sc,
                        @PathParam("organizationId") UUID organizationId,
                        @PathParam("tenantId") UUID tenantId,
                        @QueryParam("limit") int limit,
                        @QueryParam("offset") int offset) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long count = nzyme.getCotService().countAllOutputsOfTenant(organizationId, tenantId);
        List<CotOutputDetailsResponse> outputs = Lists.newArrayList();
        for (CotOutputEntry e : nzyme.getCotService().findAllOutputsOfTenant(organizationId, tenantId, limit, offset)) {
            outputs.add(CotOutputDetailsResponse.create(
                    e.uuid(),
                    e.organizationId(),
                    e.tenantId(),
                    e.name(),
                    e.description(),
                    e.leafTypeTap(),
                    e.address(),
                    e.port(),
                    e.status(),
                    e.sentMessages(),
                    e.sentBytes(),
                    e.updatedAt(),
                    e.createdAt()
            ));
        }

        return Response.ok(CotOutputListResponse.create(count, outputs)).build();
    }

    @POST
    public Response createCotOutput(@Context SecurityContext sc,
                                    @PathParam("organizationId") UUID organizationId,
                                    @PathParam("tenantId") UUID tenantId,
                                    @Valid CreateCotOutputRequest req) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if there is room in the quota.
        if (!nzyme.getQuotaService().isTenantQuotaAvailable(organizationId, tenantId, QuotaType.INTEGRATIONS_COT)) {
            return Response.status(422).build();
        }

        nzyme.getCotService().createOutput(
                organizationId,
                tenantId,
                req.name(),
                req.description(),
                req.leafTypeTap(),
                req.address(),
                req.port()
        );

        return Response.ok(Response.Status.CREATED).build();
    }

}
