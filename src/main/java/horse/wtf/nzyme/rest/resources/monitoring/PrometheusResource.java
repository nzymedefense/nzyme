package horse.wtf.nzyme.rest.resources.monitoring;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraintValidator;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import app.nzyme.plugin.rest.security.RESTSecured;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.monitoring.prometheus.PrometheusFormatter;
import horse.wtf.nzyme.monitoring.prometheus.PrometheusRegistryKeys;
import horse.wtf.nzyme.rest.authentication.PrometheusBasicAuthSecured;
import horse.wtf.nzyme.rest.requests.PrometheusConfigurationUpdateRequest;
import horse.wtf.nzyme.rest.responses.monitoring.prometheus.PrometheusConfigurationResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;


@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
@Path("/api/system/monitoring/prometheus")
public class PrometheusResource {

    private static final Logger LOG = LogManager.getLogger(PrometheusResource.class);

    @Inject
    private NzymeLeader nzyme;

    @GET
    @Path("/configuration")
    public Response configuration() {
        boolean reportEnabled = nzyme.getDatabaseCoreRegistry()
                .getValue(PrometheusRegistryKeys.REST_REPORT_ENABLED.key())
                .filter(Boolean::parseBoolean).isPresent();

        PrometheusConfigurationResponse response = PrometheusConfigurationResponse.create(
                ConfigurationEntryResponse.create(
                        PrometheusRegistryKeys.REST_REPORT_ENABLED.key(),
                        "REST Report enabled",
                        reportEnabled,
                        ConfigurationEntryResponse.ValueType.BOOLEAN,
                        PrometheusRegistryKeys.REST_REPORT_ENABLED.defaultValue().get(),
                        PrometheusRegistryKeys.REST_REPORT_ENABLED.requiresRestart(),
                        PrometheusRegistryKeys.REST_REPORT_ENABLED.constraints().get(),
                        null
                )
        );

        return Response.ok(response).build();
    }

    @PUT
    @Path("/configuration")
    public Response update(PrometheusConfigurationUpdateRequest ur) {
        if (ur.change().isEmpty()) {
            LOG.info("Empty configuration parameters.");
            return Response.status(422).build();
        }

        for (Map.Entry<String, Object> c : ur.change().entrySet()) {
            switch (c.getKey()) {
                case "prometheus_rest_report_enabled":
                    if (!ConfigurationEntryConstraintValidator.checkConstraints(PrometheusRegistryKeys.REST_REPORT_ENABLED, c)) {
                        return Response.status(422).build();
                    }
                    break;
                default:
                    LOG.info("Unknown configuration parameter [{}].", c.getKey());
                    return Response.status(422).build();
            }

            nzyme.getDatabaseCoreRegistry().setValue(c.getKey(), c.getValue().toString());
        }

        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @PrometheusBasicAuthSecured
    @Path("/metrics")
    public Response metrics() {
        PrometheusFormatter f = new PrometheusFormatter(nzyme.getMetrics());
        return Response.ok(f.format()).build();
    }

}
