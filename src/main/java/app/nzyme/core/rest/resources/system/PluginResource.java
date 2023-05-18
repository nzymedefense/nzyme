package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
