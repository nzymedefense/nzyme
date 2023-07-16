package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.db.ConnectedClientDetails;
import app.nzyme.core.dot11.db.DisconnectedClientDetails;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.dot11.clients.*;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.UUID;

@Path("/api/dot11/clients")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class Dot11ClientsResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/connected")
    public Response connectedClients(@Context SecurityContext sc,
                                     @QueryParam("minutes") int minutes,
                                     @QueryParam("taps") String taps,
                                     @QueryParam("limit") int limit,
                                     @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        long totalCount = nzyme.getDot11().countBSSIDClients(minutes, tapUuids);
        List<ConnectedClientDetailsResponse> clients = Lists.newArrayList();

        for (ConnectedClientDetails client : nzyme.getDot11().findBSSIDClients(
                minutes, tapUuids, limit, offset, Dot11.ClientOrderColumn.LAST_SEEN, OrderDirection.DESC)) {
            List<ConnectedBSSID> bssidHistory = Lists.newArrayList();
            for (String bssid : nzyme.getDot11()
                    .findBSSIDsClientWasConnectedTo(client.clientMac(), tapUuids)) {
                // Find all SSIDs this BSSID advertised.
                List<String> advertisedSSIDs = nzyme.getDot11().findSSIDsAdvertisedByBSSID(
                        bssid, tapUuids);

                bssidHistory.add(ConnectedBSSID.create(
                        bssid, nzyme.getOUIManager().lookupMac(bssid), advertisedSSIDs
                ));
            }

            List<String> probeRequests = nzyme.getDot11()
                    .findProbeRequestsOfClient(client.clientMac(), tapUuids);

            clients.add(ConnectedClientDetailsResponse.create(
                    client.clientMac(),
                    nzyme.getOUIManager().lookupMac(client.clientMac()),
                    client.lastSeen(),
                    client.bssid(),
                    nzyme.getOUIManager().lookupMac(client.bssid()),
                    probeRequests,
                    bssidHistory
            ));
        }


        return Response.ok(ConnectedClientListResponse.create(totalCount, clients)).build();
    }

    @GET
    @Path("/disconnected")
    public Response disconnectedClients(@Context SecurityContext sc,
                                        @QueryParam("minutes") int minutes,
                                        @QueryParam("taps") String taps,
                                        @QueryParam("limit") int limit,
                                        @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        long totalCount = nzyme.getDot11().countClients(minutes, tapUuids);
        List<DisconnectedClientDetailsResponse> clients = Lists.newArrayList();

        // Clients identified by probe requests.
        for (DisconnectedClientDetails client : nzyme.getDot11().findClients(
                minutes, tapUuids, limit, offset, Dot11.ClientOrderColumn.LAST_SEEN, OrderDirection.DESC)) {
            List<ConnectedBSSID> bssidHistory = Lists.newArrayList();

            for (String bssid : nzyme.getDot11()
                    .findBSSIDsClientWasConnectedTo(client.clientMac(), tapUuids)) {
                // Find all SSIDs this BSSID advertised.
                List<String> advertisedSSIDs = nzyme.getDot11().findSSIDsAdvertisedByBSSID(
                        bssid, tapUuids);

                bssidHistory.add(ConnectedBSSID.create(
                        bssid, nzyme.getOUIManager().lookupMac(bssid), advertisedSSIDs
                ));
            }

            clients.add(DisconnectedClientDetailsResponse.create(
                    client.clientMac(),
                    nzyme.getOUIManager().lookupMac(client.clientMac()),
                    client.lastSeen(),
                    client.probeRequests(),
                    bssidHistory
            ));
        }

        return Response.ok(DisconnectedClientListResponse.create(totalCount, clients)).build();
    }

}
