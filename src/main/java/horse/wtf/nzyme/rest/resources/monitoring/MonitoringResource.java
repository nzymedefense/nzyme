package horse.wtf.nzyme.rest.resources.monitoring;

import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Maps;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.monitoring.prometheus.PrometheusRegistryKeys;
import horse.wtf.nzyme.rest.responses.monitoring.MonitoringSummaryResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/api/system/monitoring")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class MonitoringResource {

    @Inject
    private NzymeLeader nzyme;

    @GET
    @Path("/summary")
    public Response summary() {
        boolean prometheusReportEnabled = nzyme.getDatabaseCoreRegistry()
                .getValue(PrometheusRegistryKeys.REST_REPORT_ENABLED.key())
                .filter(Boolean::parseBoolean).isPresent();

        Map<String, Boolean> exporters = Maps.newHashMap();
        exporters.put("prometheus", prometheusReportEnabled);

        return Response.ok(MonitoringSummaryResponse.create(exporters)).build();
    }

}
