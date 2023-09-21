package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.db.DiscoHistogramEntry;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.dot11.disco.DiscoHistogramValueResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/dot11/disco")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class Dot11DiscoResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/histogram")
    public Response histogram(@Context SecurityContext sc,
                              @QueryParam("disco_type") String type,
                              @QueryParam("minutes") int minutes,
                              @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        Dot11.DiscoType discoType;
        try {
            discoType = Dot11.DiscoType.valueOf(type.toUpperCase());
        } catch(IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Map<DateTime, DiscoHistogramEntry> histogram = Maps.newHashMap();
        for (DiscoHistogramEntry h : nzyme.getDot11().getGlobalDiscoHistogram(discoType, minutes, tapUuids)) {
            histogram.put(h.bucket(), h);
        }

        Map<DateTime, DiscoHistogramValueResponse> response = Maps.newTreeMap();
        for (int x = minutes; x != 0; x--) {
            DateTime bucket = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0).minusMinutes(x);
            DiscoHistogramEntry entry = histogram.get(bucket);
            if (entry == null) {
                response.put(bucket, DiscoHistogramValueResponse.create(bucket, 0));
            } else {
                response.put(bucket, DiscoHistogramValueResponse.create(entry.bucket(), entry.frameCount()));
            }
        }

        return Response.ok(response).build();
    }

}
