package app.nzyme.core.rest.resources.system.integrations.tenant;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.integrations.tenant.cot.CotConnectionType;
import app.nzyme.core.integrations.tenant.cot.db.CotOutputEntry;
import app.nzyme.core.quota.QuotaType;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.requests.CreateCotOutputRequest;
import app.nzyme.core.rest.requests.UpdateCotOutputRequest;
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
import java.util.Optional;
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
                    e.connectionType(),
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

    @GET
    @Path("/show/{outputId}")
    public Response one(@Context SecurityContext sc,
                        @PathParam("organizationId") UUID organizationId,
                        @PathParam("tenantId") UUID tenantId,
                        @PathParam("outputId") UUID outputId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<CotOutputEntry> output = nzyme.getCotService().findOutput(organizationId, tenantId, outputId);

        if (output.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        CotOutputEntry o = output.get();

        return Response.ok(
                CotOutputDetailsResponse.create(
                        o.uuid(),
                        o.organizationId(),
                        o.tenantId(),
                        o.connectionType(),
                        o.name(),
                        o.description(),
                        o.leafTypeTap(),
                        o.address(),
                        o.port(),
                        o.status(),
                        o.sentMessages(),
                        o.sentBytes(),
                        o.updatedAt(),
                        o.createdAt()
                )
        ).build();
    }

    @POST
    public Response create(@Context SecurityContext sc,
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

        CotConnectionType connectionType;
        try {
            connectionType = CotConnectionType.valueOf(req.connectionType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getCotService().createOutput(
                organizationId,
                tenantId,
                connectionType,
                req.name(),
                req.description(),
                req.leafTypeTap(),
                req.address(),
                req.port()
        );

        return Response.ok(Response.Status.CREATED).build();
    }


    @PUT
    @Path("/show/{outputId}")
    public Response edit(@Context SecurityContext sc,
                         @PathParam("organizationId") UUID organizationId,
                         @PathParam("tenantId") UUID tenantId,
                         @PathParam("outputId") UUID outputId,
                         @Valid UpdateCotOutputRequest req) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<CotOutputEntry> output = nzyme.getCotService().findOutput(organizationId, tenantId, outputId);

        if (output.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        CotConnectionType connectionType;
        try {
            connectionType = CotConnectionType.valueOf(req.connectionType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getCotService().updateOutput(
                output.get().id(),
                connectionType,
                req.name(),
                req.description(),
                req.leafTypeTap(),
                req.address(),
                req.port()
        );

        return Response.ok().build();
    }

    @DELETE
    @Path("/show/{outputId}")
    public Response delete(@Context SecurityContext sc,
                           @PathParam("organizationId") UUID organizationId,
                           @PathParam("tenantId") UUID tenantId,
                           @PathParam("outputId") UUID outputId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<CotOutputEntry> output = nzyme.getCotService().findOutput(organizationId, tenantId, outputId);

        if (output.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getCotService().deleteOutput(output.get().id());

        return Response.ok().build();
    }

    @PUT
    @Path("/show/{outputId}/pause")
    public Response pause(@Context SecurityContext sc,
                          @PathParam("organizationId") UUID organizationId,
                          @PathParam("tenantId") UUID tenantId,
                          @PathParam("outputId") UUID outputId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<CotOutputEntry> output = nzyme.getCotService().findOutput(organizationId, tenantId, outputId);

        if (output.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getCotService().pauseOutput(output.get().id());

        return Response.ok().build();
    }

    @PUT
    @Path("/show/{outputId}/start")
    public Response start(@Context SecurityContext sc,
                          @PathParam("organizationId") UUID organizationId,
                          @PathParam("tenantId") UUID tenantId,
                          @PathParam("outputId") UUID outputId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<CotOutputEntry> output = nzyme.getCotService().findOutput(organizationId, tenantId, outputId);

        if (output.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getCotService().startOutput(output.get().id());

        return Response.ok().build();
    }


}
