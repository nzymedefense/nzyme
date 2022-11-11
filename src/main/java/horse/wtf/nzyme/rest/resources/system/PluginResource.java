package horse.wtf.nzyme.rest.resources.system;

import app.nzyme.plugin.rest.security.RESTSecured;
import horse.wtf.nzyme.NzymeLeader;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/system/plugins")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class PluginResource {

    @Inject
    NzymeLeader nzyme;

    @GET
    @Path("/names")
    public Response getNames() {
        return Response.ok(nzyme.getInitializedPlugins()).build();
    }

}
