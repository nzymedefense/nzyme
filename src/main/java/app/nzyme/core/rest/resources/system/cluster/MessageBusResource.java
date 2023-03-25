package app.nzyme.core.rest.resources.system.cluster;

import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.rest.security.RESTSecured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/system/cluster/messagebus")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class MessageBusResource {

    private static final Logger LOG = LogManager.getLogger(MessageBusResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response findMessages(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit >250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        nzyme.getMessageBus().getAllMessages(limit, offset);

        return Response.ok().build();
    }

}
