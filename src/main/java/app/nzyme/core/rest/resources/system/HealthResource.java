package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.core.rest.responses.system.HealthIndicatorResponse;
import app.nzyme.core.rest.responses.system.HealthResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Maps;
import jakarta.ws.rs.PUT;
import org.joda.time.DateTime;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/api/system/health")
@RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("indicators")
    public Response indicators() {
        Optional<List<IndicatorStatus>> status = nzyme.getHealthMonitor().getIndicatorStatus();

        Map<String, HealthIndicatorResponse> indicators = Maps.newHashMap();
        if (status.isPresent()) {
            for (IndicatorStatus s : status.get()) {
                indicators.put(s.indicatorId(), HealthIndicatorResponse.create(
                        s.indicatorId(),
                        s.indicatorName(),
                        s.resultLevel().toString().toUpperCase(),
                        s.lastChecked(),
                        s.lastChecked().isBefore(DateTime.now().minusMinutes(2)),
                        s.active()
                ));
            }
        }

        return Response.ok(HealthResponse.create(indicators)).build();
    }

    @PUT
    @Path("/indicators/configuration")
    public Response updateIndicatorConfig(Map<String, Map<String, String>> config) {
        for (Map.Entry<String, Map<String, String>> c : config.entrySet()) {
            if (c.getValue().containsKey("active")) {
                boolean active = Boolean.parseBoolean(c.getValue().get("active"));
                nzyme.getHealthMonitor().updateIndicatorActivationState(c.getKey(), active);
            }
        }

        return Response.ok().build();
    }

}
