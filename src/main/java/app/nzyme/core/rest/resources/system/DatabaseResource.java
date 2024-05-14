package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.dot11.Dot11RegistryKeys;
import app.nzyme.core.rest.requests.RetentionTimeConfigurationUpdateRequest;
import app.nzyme.core.rest.responses.system.DatabaseSummaryResponse;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraintValidator;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryValueType;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;

import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/system/database")
@RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
@Produces(MediaType.APPLICATION_JSON)
public class DatabaseResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response summary() {
        DatabaseImpl db = (DatabaseImpl) nzyme.getDatabase();
        long totalSize = db.getTotalSize();
        long ethernetSize = db.getTableSize("dns_log")
                + db.getTableSize("dns_entropy_log")
                + db.getTableSize("dns_pairs")
                + db.getTableSize("dns_statistics");

        long dot11Size = db.getTableSize("dot11_bssids")
                + db.getTableSize("dot11_channels")
                + db.getTableSize("dot11_fingerprints")
                + db.getTableSize("dot11_ssids")
                + db.getTableSize("dot11_infrastructure_types")
                + db.getTableSize("dot11_bssid_clients")
                + db.getTableSize("dot11_rates")
                + db.getTableSize("dot11_clients")
                + db.getTableSize("dot11_client_probereq_ssids")
                + db.getTableSize("dot11_channel_histograms")
                + db.getTableSize("dot11_disco_activity")
                + db.getTableSize("dot11_disco_activity_receivers");

        int retentionTime = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                .getValue(Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.key())
                .orElse(Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING"))
        );

        ConfigurationEntryResponse dot11RetentionTimeConfig = ConfigurationEntryResponse.create(
                Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.key(),
                "Retention Time (in days)",
                retentionTime,
                ConfigurationEntryValueType.NUMBER,
                Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.defaultValue().orElse(null),
                Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.requiresRestart(),
                Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.constraints().orElse(Lists.newArrayList()),
                "retention-time"
        );

        return Response.ok(DatabaseSummaryResponse.create(
                totalSize, ethernetSize, dot11Size, dot11RetentionTimeConfig
        )).build();
    }

    @PUT
    @Path("/retention")
    public Response updateRetentionTimeConfiguration(@Valid RetentionTimeConfigurationUpdateRequest ur) {
        for (Map.Entry<String, Object> c : ur.change().entrySet()) {
            switch (c.getKey()) {
                case "dot11_retention_time_days":
                    if (!ConfigurationEntryConstraintValidator.checkConstraints(Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS, c)) {
                        return Response.status(422).build();
                    }
                    break;
            }

            nzyme.getDatabaseCoreRegistry().setValue(c.getKey(), c.getValue().toString());
        }

        return Response.ok().build();
    }

}
