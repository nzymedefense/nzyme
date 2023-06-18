package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.BSSIDEntry;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.dot11.BSSIDDetailsResponse;
import app.nzyme.core.rest.responses.dot11.BSSIDListResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.UUID;

@Path("/api/dot11/networks")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class Dot11Resource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/bssids")
    public Response bssids(@Context SecurityContext sc,
                           @QueryParam("minutes") int minutes,
                           @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        List<BSSIDDetailsResponse> bssids = Lists.newArrayList();
        for (BSSIDEntry bssid : nzyme.getDot11().findBSSIDs(minutes, tapUuids)) {
            List<String> fingerprints = nzyme.getDot11().findFingerprintsOfBSSID(minutes, bssid.bssid());
            List<String> advertisedSSIDNames = nzyme.getDot11().findAdvertisedSSIDNamesOfBSSID(minutes, bssid.bssid());

            bssids.add(BSSIDDetailsResponse.create(
                    bssid.bssid(),
                    bssid.signalStrengthAverage(),
                    bssid.lastSeen(),
                    fingerprints,
                    advertisedSSIDNames,
                    bssid.hiddenSSIDFrames() > 0
            ));
        }

        return Response.ok(BSSIDListResponse.create(bssids)).build();
    }

}
