package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.context.db.MacAddressContextEntry;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.Dot11RegistryKeys;
import app.nzyme.core.dot11.db.*;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.dot11.Dot11MacAddressContextResponse;
import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
import app.nzyme.core.rest.responses.dot11.TapBasedSignalStrengthResponse;
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
            Optional<MacAddressContextEntry> clientContext = nzyme.getContextService().findMacAddressContext(
                    client.clientMac(),
                    authenticatedUser.getOrganizationId(),
                    authenticatedUser.getTenantId()
            );

            List<ConnectedBSSID> bssidHistory = Lists.newArrayList();
            for (String bssid : nzyme.getDot11()
                    .findBSSIDsClientWasConnectedTo(client.clientMac(), tapUuids)) {
                Optional<MacAddressContextEntry> bssidContext = nzyme.getContextService().findMacAddressContext(
                        bssid,
                        authenticatedUser.getOrganizationId(),
                        authenticatedUser.getTenantId()
                );

                // Find all SSIDs this BSSID advertised.
                List<String> advertisedSSIDs = nzyme.getDot11().findSSIDsAdvertisedByBSSID(
                        bssid, tapUuids);

                bssidHistory.add(ConnectedBSSID.create(
                        Dot11MacAddressResponse.create(
                                bssid,
                                nzyme.getOUIManager().lookupMac(bssid),
                                bssidContext.map(macAddressContextEntry ->
                                                Dot11MacAddressContextResponse.create(
                                                        macAddressContextEntry.name(),
                                                        macAddressContextEntry.description()
                                                ))
                                        .orElse(null)
                        ),
                        advertisedSSIDs
                ));
            }

            List<String> probeRequests = nzyme.getDot11()
                    .findProbeRequestsOfClient(client.clientMac(), tapUuids);

            Optional<MacAddressContextEntry> clientBssidContext = nzyme.getContextService().findMacAddressContext(
                    client.bssid(),
                    authenticatedUser.getOrganizationId(),
                    authenticatedUser.getTenantId()
            );

            connectedClients.add(ConnectedClientDetailsResponse.create(
                    Dot11MacAddressResponse.create(
                            client.clientMac(),
                            nzyme.getOUIManager().lookupMac(client.clientMac()),
                            clientContext.map(macAddressContextEntry ->
                                            Dot11MacAddressContextResponse.create(
                                                    macAddressContextEntry.name(),
                                                    macAddressContextEntry.description()
                                            ))
                                    .orElse(null)
                    ),
                    client.lastSeen(),
                    Dot11MacAddressResponse.create(
                            client.bssid(),
                            nzyme.getOUIManager().lookupMac(client.bssid()),
                            clientBssidContext.map(macAddressContextEntry ->
                                            Dot11MacAddressContextResponse.create(
                                                    macAddressContextEntry.name(),
                                                    macAddressContextEntry.description()
                                            ))
                                    .orElse(null)
                    ),
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
            Optional<MacAddressContextEntry> clientContext = nzyme.getContextService().findMacAddressContext(
                    client.clientMac(),
                    authenticatedUser.getOrganizationId(),
                    authenticatedUser.getTenantId()
            );

            List<ConnectedBSSID> bssidHistory = Lists.newArrayList();

            for (String bssid : nzyme.getDot11()
                    .findBSSIDsClientWasConnectedTo(client.clientMac(), tapUuids)) {
                Optional<MacAddressContextEntry> bssidContext = nzyme.getContextService().findMacAddressContext(
                        bssid,
                        authenticatedUser.getOrganizationId(),
                        authenticatedUser.getTenantId()
                );

                // Find all SSIDs this BSSID advertised.
                List<String> advertisedSSIDs = nzyme.getDot11().findSSIDsAdvertisedByBSSID(
                        bssid, tapUuids);

                bssidHistory.add(ConnectedBSSID.create(
                        Dot11MacAddressResponse.create(
                                bssid,
                                nzyme.getOUIManager().lookupMac(bssid),
                                bssidContext.map(macAddressContextEntry ->
                                                Dot11MacAddressContextResponse.create(
                                                        macAddressContextEntry.name(),
                                                        macAddressContextEntry.description()
                                                ))
                                        .orElse(null)
                        ),
                        advertisedSSIDs
                ));
            }

            disconnectedClients.add(DisconnectedClientDetailsResponse.create(
                    Dot11MacAddressResponse.create(
                            client.clientMac(),
                            nzyme.getOUIManager().lookupMac(client.clientMac()),
                            clientContext.map(macAddressContextEntry ->
                                            Dot11MacAddressContextResponse.create(
                                                    macAddressContextEntry.name(),
                                                    macAddressContextEntry.description()
                                            ))
                                    .orElse(null)
                    ),
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
    public Response client(@Context SecurityContext sc,
                           @PathParam("clientMac") @NotEmpty String clientMac,
                           @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        Optional<ClientDetails> client = nzyme.getDot11().findMergedConnectedOrDisconnectedClient(
                clientMac, tapUuids, authenticatedUser);

        if (client.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ClientDetails c = client.get();

        // Recent signal strength by tap.
        List<TapBasedSignalStrengthResponse> connectedSignalStrengthsByTap = Lists.newArrayList();
        for (TapBasedSignalStrengthResult ssr : nzyme.getDot11()
                .findBssidClientSignalStrengthPerTap(c.mac(), 15, tapUuids)) {
            connectedSignalStrengthsByTap.add(TapBasedSignalStrengthResponse.create(
                    ssr.tapUuid(), ssr.tapName(), ssr.signalStrength()
            ));
        }
        List<TapBasedSignalStrengthResponse> disconnectedSignalStrengthsByTap = Lists.newArrayList();
        for (TapBasedSignalStrengthResult ssr : nzyme.getDot11()
                .findDisconnectedClientSignalStrengthPerTap(c.mac(), 15, tapUuids)) {
            disconnectedSignalStrengthsByTap.add(TapBasedSignalStrengthResponse.create(
                    ssr.tapUuid(), ssr.tapName(), ssr.signalStrength()
            ));
        }

        // Disconnection frames.
        List<DiscoHistogramEntry> discos = nzyme.getDot11().getDiscoHistogram(
                Dot11.DiscoType.DISCONNECTION,
                24 * 60,
                tapUuids,
                List.of(c.mac())
        );

        Map<DateTime, ClientActivityHistogramEntry> connectedHistogram = Maps.newHashMap();
        Map<DateTime, ClientActivityHistogramEntry> disconnectedHistogram = Maps.newHashMap();
        Map<DateTime, DiscoHistogramEntry> discoActivityHistogram = Maps.newHashMap();

        for (ClientActivityHistogramEntry ce : c.connectedFramesHistogram()) {
            connectedHistogram.put(ce.bucket(), ce);
        }

        for (ClientActivityHistogramEntry ce : c.disconnectedFramesHistogram()) {
            disconnectedHistogram.put(ce.bucket(), ce);
        }

        for (DiscoHistogramEntry disco : discos) {
            discoActivityHistogram.put(disco.bucket(), disco);
        }

        Map<DateTime, ClientActivityHistogramValueResponse> activityHistogram = Maps.newTreeMap();
        for (int x = 24*60; x != 0; x--) {
            DateTime bucket = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0).minusMinutes(x);

            ClientActivityHistogramEntry connected = connectedHistogram.get(bucket);
            ClientActivityHistogramEntry disconnected = disconnectedHistogram.get(bucket);
            DiscoHistogramEntry disco = discoActivityHistogram.get(bucket);

            long connectedFrames;
            if (connected != null) {
                connectedFrames = connected.frames();
            } else {
                connectedFrames = 0L;
            }

            long disconnectedFrames;
            if (disconnected != null) {
                disconnectedFrames = disconnected.frames();
            } else {
                disconnectedFrames = 0L;
            }

            long discoActivityFrames;
            if (disco != null) {
                discoActivityFrames = disco.frameCount();
            } else {
                discoActivityFrames = 0L;
            }

            activityHistogram.put(bucket, ClientActivityHistogramValueResponse.create(
                    bucket,
                    connectedFrames+disconnectedFrames,
                    connectedFrames,
                    disconnectedFrames,
                    discoActivityFrames
            ));
        }

        int dataRetentionDays = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                .getValue(Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.key())
                .orElse(Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING"))
        );

        Optional<MacAddressContextEntry> macContext = nzyme.getContextService().findMacAddressContext(
                c.mac(),
                authenticatedUser.getOrganizationId(),
                authenticatedUser.getTenantId()
        );

        return Response.ok(ClientDetailsResponse.create(
                Dot11MacAddressResponse.create(
                        c.mac(),
                        nzyme.getOUIManager().lookupMac(c.mac()),
                        macContext.map(macAddressContextEntry ->
                                        Dot11MacAddressContextResponse.create(
                                                macAddressContextEntry.name(),
                                                macAddressContextEntry.description()
                                        ))
                                .orElse(null)
                ),
                c.connectedBSSID(),
                c.connectedBSSIDHistory(),
                c.firstSeen(),
                c.lastSeen(),
                c.probeRequests(),
                activityHistogram,
                connectedSignalStrengthsByTap,
                disconnectedSignalStrengthsByTap,
                dataRetentionDays
        )).build();
    }

}
