package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.db.ClientDetails;
import app.nzyme.core.dot11.db.ClientHistogramEntry;
import app.nzyme.core.dot11.db.ConnectedClientDetails;
import app.nzyme.core.dot11.db.DisconnectedClientDetails;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.dot11.clients.*;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;

import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Path("/api/dot11/clients")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class Dot11ClientsResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/histograms")
    public Response histograms(@Context SecurityContext sc,
                               @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        final int minutes = 24*60;

        List<String> macAddressesOfAllConnectedClients = nzyme.getDot11()
                .findMacAddressesOfAllBSSIDClients(minutes, tapUuids);

        Map<DateTime, ClientHistogramValueResponse> connected = Maps.newTreeMap();
        Map<DateTime, ClientHistogramValueResponse> disconnected = Maps.newTreeMap();

        Map<DateTime, ClientHistogramEntry> connectedData = Maps.newHashMap();
        for (ClientHistogramEntry entry : nzyme.getDot11().getConnectedClientHistogram(minutes, tapUuids)) {
            connectedData.put(entry.bucket(), entry);
        }

        Map<DateTime, ClientHistogramEntry> disconnectedData = Maps.newHashMap();
        for (ClientHistogramEntry entry : nzyme.getDot11().getClientHistogram(
                minutes, tapUuids, macAddressesOfAllConnectedClients)) {
            disconnectedData.put(entry.bucket(), entry);
        }

        for (int x = minutes; x != 0; x--) {
            DateTime bucket = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0).minusMinutes(x);
            ClientHistogramEntry connectedEntry = connectedData.get(bucket);
            ClientHistogramEntry disconnectedEntry = disconnectedData.get(bucket);

            if (connectedEntry == null) {
                connected.put(bucket, ClientHistogramValueResponse.create(bucket, 0));
            } else {
                connected.put(connectedEntry.bucket(), ClientHistogramValueResponse.create(
                        connectedEntry.bucket(), connectedEntry.clientCount()
                ));
            }

            if (disconnectedEntry == null) {
                disconnected.put(bucket, ClientHistogramValueResponse.create(bucket, 0));
            } else {
                disconnected.put(disconnectedEntry.bucket(), ClientHistogramValueResponse.create(
                        disconnectedEntry.bucket(), disconnectedEntry.clientCount()
                ));
            }
        }

        return Response.ok(ClientHistogramsResponse.create(connected, disconnected)).build();
    }

    @GET
    public Response allClients(@Context SecurityContext sc,
                               @QueryParam("minutes") int minutes,
                               @QueryParam("taps") String taps,
                               @QueryParam("connectedLimit") int connectedLimit,
                               @QueryParam("connectedOffset") int connectedOffset,
                               @QueryParam("disconnectedLimit") int disconnectedLimit,
                               @QueryParam("disconnectedOffset") int disconnectedOffset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        // Connected clients.
        long connectedCount = nzyme.getDot11().countBSSIDClients(minutes, tapUuids);
        List<ConnectedClientDetailsResponse> connectedClients = Lists.newArrayList();
        List<String> macAddressesOfAllConnectedClients = nzyme.getDot11()
                .findMacAddressesOfAllBSSIDClients(minutes, tapUuids);

        for (ConnectedClientDetails client : nzyme.getDot11().findBSSIDClients(
                minutes, tapUuids, connectedLimit, connectedOffset,
                Dot11.ClientOrderColumn.LAST_SEEN, OrderDirection.DESC)) {
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

            connectedClients.add(ConnectedClientDetailsResponse.create(
                    client.clientMac(),
                    nzyme.getOUIManager().lookupMac(client.clientMac()),
                    client.lastSeen(),
                    client.bssid(),
                    nzyme.getOUIManager().lookupMac(client.bssid()),
                    probeRequests,
                    bssidHistory
            ));
        }

        // Disconnected clients.
        long disconnectedCount = nzyme.getDot11().countClients(minutes, tapUuids);
        List<DisconnectedClientDetailsResponse> disconnectedClients = Lists.newArrayList();

        for (DisconnectedClientDetails client : nzyme.getDot11().findClients(
                minutes, tapUuids, macAddressesOfAllConnectedClients, disconnectedLimit, disconnectedOffset,
                Dot11.ClientOrderColumn.LAST_SEEN, OrderDirection.DESC)) {
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

            disconnectedClients.add(DisconnectedClientDetailsResponse.create(
                    client.clientMac(),
                    nzyme.getOUIManager().lookupMac(client.clientMac()),
                    client.lastSeen(),
                    client.probeRequests(),
                    bssidHistory
            ));
        }

        return Response.ok(ClientListResponse.create(
                ConnectedClientListResponse.create(connectedCount, connectedClients),
                DisconnectedClientListResponse.create(disconnectedCount, disconnectedClients)
        )).build();
    }

    @GET
    @Path("/show/{clientMac}")
    public Response histograms(@Context SecurityContext sc,
                               @PathParam("clientMac") @NotEmpty String clientMac,
                               @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        Optional<ClientDetails> client = nzyme.getDot11().findMergedConnectedOrDisconnectedClient(
                clientMac, tapUuids);

        if (client.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ClientDetails c = client.get();
        return Response.ok(ClientDetailsResponse.create(
                c.mac(),
                c.macOui(),
                c.connectedBSSIDs(),
                c.lastSeen(),
                c.probeRequests()
        )).build();
    }

}
