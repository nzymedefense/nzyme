package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.BSSIDSummary;
import app.nzyme.core.dot11.db.SSIDSummary;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.dot11.BSSIDSummaryDetailsResponse;
import app.nzyme.core.rest.responses.dot11.BSSIDListResponse;
import app.nzyme.core.rest.responses.dot11.SSIDListResponse;
import app.nzyme.core.rest.responses.dot11.SSIDSummaryDetailsResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;

import javax.inject.Inject;
import javax.ws.rs.*;
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

        List<BSSIDSummaryDetailsResponse> bssids = Lists.newArrayList();
        for (BSSIDSummary bssid : nzyme.getDot11().findBSSIDs(minutes, tapUuids)) {
            List<String> fingerprints = nzyme.getDot11().findFingerprintsOfBSSID(minutes, bssid.bssid(), tapUuids);

            bssids.add(BSSIDSummaryDetailsResponse.create(
                    bssid.bssid(),
                    nzyme.getOUIManager().lookupBSSID(bssid.bssid()),
                    bssid.securityProtocols(),
                    bssid.signalStrengthAverage(),
                    bssid.lastSeen(),
                    fingerprints,
                    bssid.ssids(),
                    bssid.hiddenSSIDFrames() > 0
            ));
        }

        return Response.ok(BSSIDListResponse.create(bssids)).build();
    }

    @GET
    @Path("/bssids/show/{bssid}/ssids")
    public Response bssidSSIDs(@Context SecurityContext sc,
                               @PathParam("bssid") String bssid,
                               @QueryParam("minutes") int minutes,
                               @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        if (!nzyme.getDot11().bssidExist(bssid, minutes, tapUuids)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<SSIDSummaryDetailsResponse> ssids = Lists.newArrayList();
        for (SSIDSummary ssid : nzyme.getDot11().findSSIDsOfBSSID(minutes, bssid, tapUuids)) {
            ssids.add(SSIDSummaryDetailsResponse.create(
                    ssid.ssid(),
                    ssid.signalStrengthAverage(),
                    ssid.securityProtocols(),
                    ssid.isWps(),
                    ssid.lastSeen()
            ));
        }

        return Response.ok(SSIDListResponse.create(ssids)).build();
    }

}
