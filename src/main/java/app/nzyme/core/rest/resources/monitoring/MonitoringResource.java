package app.nzyme.core.rest.resources.monitoring;

import app.nzyme.plugin.RegistryCryptoException;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Maps;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.monitoring.exporters.prometheus.PrometheusRegistryKeys;
import app.nzyme.core.rest.responses.monitoring.MonitoringSummaryResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/api/system/monitoring")
@RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
@Produces(MediaType.APPLICATION_JSON)
public class MonitoringResource {

    @Inject
    private NzymeNode nzyme;

    private static final Logger LOG = LogManager.getLogger(MonitoringResource.class);

    @GET
    @Path("/summary")
    public Response summary() {
        boolean prometheusReportEnabled;
        try {
            prometheusReportEnabled = nzyme.getDatabaseCoreRegistry()
                    .getValue(PrometheusRegistryKeys.REST_REPORT_ENABLED.key())
                    .filter(Boolean::parseBoolean)
                    .isPresent()
                    && nzyme.getDatabaseCoreRegistry().getValue(PrometheusRegistryKeys.REST_REPORT_USERNAME.key())
                    .isPresent()
                    && nzyme.getDatabaseCoreRegistry().getEncryptedValue(PrometheusRegistryKeys.REST_REPORT_PASSWORD.key())
                    .isPresent();
        } catch(RegistryCryptoException e) {
            LOG.error("Could not decrypt encrypted registry value", e);
            return Response.serverError().build();
        }

        Map<String, Boolean> exporters = Maps.newHashMap();
        exporters.put("prometheus", prometheusReportEnabled);

        return Response.ok(MonitoringSummaryResponse.create(exporters)).build();
    }

}
