package app.nzyme.core.rest.resources.system.integrations;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.integrations.geoip.GeoIpService;
import app.nzyme.core.rest.responses.integrations.geoip.GeoIpSummaryResponse;
import app.nzyme.plugin.rest.security.RESTSecured;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/system/integrations/geoip")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class GeoIpIntegrationsResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response summary() {
        GeoIpService geo = nzyme.getGeoIpService();

        return Response.ok(GeoIpSummaryResponse.create(geo.getAdapterName())).build();
    }

}
