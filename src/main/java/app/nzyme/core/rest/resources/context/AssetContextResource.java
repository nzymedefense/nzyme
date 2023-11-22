package app.nzyme.core.rest.resources.context;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.Subsystem;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.requests.CreateMacAddressContextRequest;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/context")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class AssetContextResource extends UserAuthenticatedResource {

    @Inject
    private NzymeNode nzyme;

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "mac_aliases_manage" })
    @Path("/mac")
    public Response macs(@Context SecurityContext sc, CreateMacAddressContextRequest req) {
        if (!passedTenantDataAccessible(sc, req.organizationId(), req.tenantId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getContextService().createMacAddressContext(
                req.macAddress(),
                Subsystem.valueOf(req.subsystem().toUpperCase()),
                req.name(),
                req.description(),
                req.notes(),
                req.organizationId(),
                req.tenantId()
        );

        return Response.status(Response.Status.CREATED).build();
    }


}
