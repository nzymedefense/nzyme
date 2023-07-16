package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/system/plugins")
@RESTSecured(PermissionLevel.ANY)
@Produces(MediaType.APPLICATION_JSON)
public class PluginResource {

    @Inject
    NzymeNode nzyme;

    @GET
    @Path("/names")
    public Response getNames() {
        return Response.ok(nzyme.getInitializedPlugins()).build();
    }

}
