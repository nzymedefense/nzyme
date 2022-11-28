package horse.wtf.nzyme.rest.resources.monitoring;

import horse.wtf.nzyme.NzymeLeader;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/monitoring/prometheus")
@Produces(MediaType.APPLICATION_JSON)
public class PrometheusResource {

    @Inject
    private NzymeLeader nzyme;

    @GET
    @Produces("text/plain")
    @Path("/metrics")
    public Response metrics() {
        return null;
    }

}
