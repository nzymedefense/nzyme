package horse.wtf.nzyme.rest.resources.monitoring;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.monitoring.prometheus.PrometheusFormatter;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/monitoring/prometheus")
public class PrometheusResource {

    @Inject
    private NzymeLeader nzyme;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/metrics")
    public Response metrics() {
        PrometheusFormatter f = new PrometheusFormatter(nzyme.getMetrics());

        return Response.ok(f.format()).build();
    }

}
