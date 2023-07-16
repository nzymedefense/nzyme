package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.rest.responses.system.DatabaseSummaryResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
        long ethernetSize = db.getTableSize("dns_nxdomains_log")
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
                + db.getTableSize("dot11_channel_histograms");

        return Response.ok(DatabaseSummaryResponse.create(
                totalSize, ethernetSize, dot11Size
        )).build();
    }

}
