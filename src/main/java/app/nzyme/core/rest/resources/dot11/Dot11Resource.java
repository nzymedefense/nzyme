package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.Dot11;
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
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/dot11/networks")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class Dot11Resource extends TapDataHandlingResource {

    private static final Logger LOG = LogManager.getLogger(Dot11Resource.class);

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
            bssids.add(BSSIDSummaryDetailsResponse.create(
                    bssid.bssid(),
                    nzyme.getOUIManager().lookupBSSID(bssid.bssid()),
                    bssid.securityProtocols(),
                    bssid.signalStrengthAverage(),
                    bssid.lastSeen(),
                    bssid.fingerprints(),
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

        List<SSIDSummary> ssids = nzyme.getDot11().findSSIDsOfBSSID(minutes, bssid, tapUuids);

        // Find main active channel per SSID.
        Map<String, Map<Integer, Long>> activeChannels = Maps.newHashMap();
        for (SSIDSummary ssid : ssids) {
            if (!activeChannels.containsKey(ssid.ssid())) {
                activeChannels.put(ssid.ssid(), Maps.newHashMap());
            }
            Map<Integer, Long> ssidChannels = activeChannels.get(ssid.ssid());
            ssidChannels.put(ssid.frequency(), ssid.totalFrames());
        }
        Map<String, Integer> mostActiveChannels = Maps.newHashMap();
        for (Map.Entry<String, Map<Integer, Long>> channel : activeChannels.entrySet()) {
            String ssid = channel.getKey();
            int mostActiveChannel = 0;
            int highestCount = 0;
            for (Map.Entry<Integer, Long> count : channel.getValue().entrySet()) {
                if (count.getValue() > highestCount) {
                    mostActiveChannel = count.getKey();
                }
            }

            mostActiveChannels.put(ssid, mostActiveChannel);
        }

        List<SSIDSummaryDetailsResponse> ssidsResult = Lists.newArrayList();
        for (SSIDSummary ssid : ssids) {
            ssidsResult.add(SSIDSummaryDetailsResponse.create(
                    ssid.ssid(),
                    ssid.frequency(),
                    Dot11.frequencyToChannel(ssid.frequency()),
                    ssid.signalStrengthAverage(),
                    ssid.totalFrames(),
                    ssid.totalBytes(),
                    mostActiveChannels.get(ssid.ssid()).equals(ssid.frequency()),
                    ssid.securityProtocols(),
                    ssid.isWps(),
                    ssid.lastSeen()
            ));
        }

        return Response.ok(SSIDListResponse.create(ssidsResult)).build();
    }

    @GET
    @Path("/bssids/show/{bssid}/ssids/show/{ssid}/channels/show/{channel}/")
    public Response bssids(@Context SecurityContext sc,
                           @PathParam("bssid") String bssid,
                           @PathParam("ssid") String ssid,
                           @PathParam("channel") int channel,
                           @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);



    }

}
