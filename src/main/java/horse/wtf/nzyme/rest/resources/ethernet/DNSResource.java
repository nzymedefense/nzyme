package horse.wtf.nzyme.rest.resources.ethernet;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.rest.authentication.RESTSecured;
import horse.wtf.nzyme.rest.resources.NetworksResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/ethernet/dns")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured
public class DNSResource {

    private static final Logger LOG = LogManager.getLogger(NetworksResource.class);

    @Inject
    private NzymeLeader nzyme;

    @GET
    @Path("/statistics")
    public Response statistics() {

    }

}
