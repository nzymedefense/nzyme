package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.context.db.MacAddressContextEntry;
import app.nzyme.core.context.db.MacAddressTransparentContextEntry;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.Dot11RegistryKeys;
import app.nzyme.core.dot11.db.*;
import app.nzyme.core.rest.RestHelpers;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.misc.CategorizedTransparentContextData;
import app.nzyme.core.rest.responses.dot11.Dot11MacAddressContextResponse;
import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
import app.nzyme.core.rest.responses.shared.TapBasedSignalStrengthResponse;
import app.nzyme.core.rest.responses.dot11.clients.*;
import app.nzyme.core.shared.db.TapBasedSignalStrengthResult;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.TimeRangeFactory;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;

import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
    @Path("/connected")
    public Response connectedClients(@Context SecurityContext sc,
                                     @QueryParam("time_range") @Valid String timeRangeParameter,
                                     @QueryParam("taps") String taps,
                                     @QueryParam("limit") int limit,
                                     @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        // Connected clients.
        long connectedCount = nzyme.getDot11().countBSSIDClients(timeRange, tapUuids);
        List<ConnectedClientDetailsResponse> connectedClients = Lists.newArrayList();

        for (ConnectedClientDetails client : nzyme.getDot11().findBSSIDClients(
                timeRange, tapUuids, limit, offset,
                Dot11.ClientOrderColumn.LAST_SEEN, OrderDirection.DESC)) {
            Optional<MacAddressContextEntry> clientContext = nzyme.getContextService().findMacAddressContext(
                    client.clientMac(),
                    authenticatedUser.getOrganizationId(),
                    authenticatedUser.getTenantId()
            );

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
                            nzyme.getOuiService().lookup(client.clientMac()).orElse(null),
                            Tools.macAddressIsRandomized(client.clientMac()),
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
                            nzyme.getOuiService().lookup(client.bssid()).orElse(null),
                            null,
                            clientBssidContext.map(macAddressContextEntry ->
                                            Dot11MacAddressContextResponse.create(
                                                    macAddressContextEntry.name(),
                                                    macAddressContextEntry.description()
                                            ))
                                    .orElse(null)
                    ),
                    probeRequests
            ));
        }

        return Response.ok(ConnectedClientListResponse.create(connectedCount, connectedClients)).build();
    }

    @GET
    @Path("/disconnected")
    public Response disconnectedClients(@Context SecurityContext sc,
                                        @QueryParam("skip_randomized") boolean skipRandomized,
                                        @QueryParam("time_range") @Valid String timeRangeParameter,
                                        @QueryParam("taps") String taps,
                                        @QueryParam("limit") int limit,
                                        @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        List<String> macAddressesOfAllConnectedClients = nzyme.getDot11()
                .findMacAddressesOfAllBSSIDClients(timeRange, tapUuids);

        // Disconnected clients.
        long disconnectedCount = nzyme.getDot11().countClients(timeRange, skipRandomized, tapUuids);
        List<DisconnectedClientDetailsResponse> disconnectedClients = Lists.newArrayList();

        for (DisconnectedClientDetails client : nzyme.getDot11().findClients(
                timeRange, tapUuids, macAddressesOfAllConnectedClients, skipRandomized, limit, offset,
                Dot11.ClientOrderColumn.LAST_SEEN, OrderDirection.DESC)) {
            Optional<MacAddressContextEntry> clientContext = nzyme.getContextService().findMacAddressContext(
                    client.clientMac(),
                    authenticatedUser.getOrganizationId(),
                    authenticatedUser.getTenantId()
            );

            disconnectedClients.add(DisconnectedClientDetailsResponse.create(
                    Dot11MacAddressResponse.create(
                            client.clientMac(),
                            nzyme.getOuiService().lookup(client.clientMac()).orElse(null),
                            Tools.macAddressIsRandomized(client.clientMac()),
                            clientContext.map(macAddressContextEntry ->
                                            Dot11MacAddressContextResponse.create(
                                                    macAddressContextEntry.name(),
                                                    macAddressContextEntry.description()
                                            ))
                                    .orElse(null)
                    ),
                    client.lastSeen(),
                    client.probeRequests()
            ));
        }

        return Response.ok(DisconnectedClientListResponse.create(disconnectedCount, disconnectedClients)).build();
    }

    @GET
    @Path("/connected/histogram")
    public Response connectedHistogram(@Context SecurityContext sc,
                                       @QueryParam("time_range") @Valid String timeRangeParameter,
                                       @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, ClientHistogramValueResponse> connected = Maps.newTreeMap();
        for (ClientHistogramEntry entry : nzyme.getDot11().getConnectedClientHistogram(timeRange, bucketing, tapUuids)) {
            connected.put(entry.bucket(), ClientHistogramValueResponse.create(
                    entry.bucket(), entry.clientCount()
            ));
        }

        return Response.ok(ClientHistogramResponse.create(connected)).build();
    }

    @GET
    @Path("/disconnected/histogram")
    public Response disconnectedHistogram(@Context SecurityContext sc,
                                          @QueryParam("skip_randomized") boolean skipRandomized,
                                          @QueryParam("time_range") @Valid String timeRangeParameter,
                                          @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        List<String> macAddressesOfAllConnectedClients = nzyme.getDot11()
                .findMacAddressesOfAllBSSIDClients(timeRange, tapUuids);

        Map<DateTime, ClientHistogramValueResponse> disconnected = Maps.newTreeMap();
        for (ClientHistogramEntry entry : nzyme.getDot11().getDisconnectedClientHistogram(
                timeRange, skipRandomized, bucketing, tapUuids, macAddressesOfAllConnectedClients)) {
            disconnected.put(entry.bucket(), ClientHistogramValueResponse.create(
                    entry.bucket(), entry.clientCount()
            ));;
        }

        return Response.ok(ClientHistogramResponse.create(disconnected)).build();
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
                .findBssidClientSignalStrengthPerTap(c.mac(), TimeRangeFactory.fifteenMinutes(), tapUuids)) {
            connectedSignalStrengthsByTap.add(TapBasedSignalStrengthResponse.create(
                    ssr.tapUuid(), ssr.tapName(), ssr.signalStrength()
            ));
        }
        List<TapBasedSignalStrengthResponse> disconnectedSignalStrengthsByTap = Lists.newArrayList();
        for (TapBasedSignalStrengthResult ssr : nzyme.getDot11()
                .findDisconnectedClientSignalStrengthPerTap(c.mac(),  TimeRangeFactory.fifteenMinutes(), tapUuids)) {
            disconnectedSignalStrengthsByTap.add(TapBasedSignalStrengthResponse.create(
                    ssr.tapUuid(), ssr.tapName(), ssr.signalStrength()
            ));
        }

        // Disconnection frames.
        List<DiscoHistogramEntry> discos = nzyme.getDot11().getDiscoHistogram(
                Dot11.DiscoType.DISCONNECTION,
                TimeRangeFactory.oneDay(),
                Bucketing.getConfig(TimeRangeFactory.oneDay()),
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

        CategorizedTransparentContextData transparentContext;
        if (macContext.isPresent()) {
            transparentContext =  RestHelpers.transparentContextDataToResponses(
                    nzyme.getContextService().findTransparentMacAddressContext(macContext.get().id())
            );
        } else {
            transparentContext = CategorizedTransparentContextData.create(Lists.newArrayList(), Lists.newArrayList());
        }

        return Response.ok(ClientDetailsResponse.create(
                Dot11MacAddressResponse.create(
                        c.mac(),
                        nzyme.getOuiService().lookup(c.mac()).orElse(null),
                        Tools.macAddressIsRandomized(c.mac()),
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
                transparentContext.ipAddresses(),
                transparentContext.hostnames(),
                c.probeRequests(),
                activityHistogram,
                connectedSignalStrengthsByTap,
                disconnectedSignalStrengthsByTap,
                dataRetentionDays
        )).build();
    }

    @GET
    @Path("/show/{clientMac}/histogram/signal/connected")
    public Response connectedSignalStrengthHistogram(@Context SecurityContext sc,
                                                     @PathParam("clientMac") @NotEmpty String clientMac,
                                                     @QueryParam("time_range") @Valid String timeRangeParameter,
                                                     @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        if (tapUuids.size() != 1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        UUID tapUUID = tapUuids.get(0);

        Optional<ClientDetails> client = nzyme.getDot11().findMergedConnectedOrDisconnectedClient(
                clientMac, tapUuids, authenticatedUser
        );

        if (client.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<ClientSignalStrengthResponse> histogram = Lists.newArrayList();
        for (ClientSignalStrengthResult r : nzyme.getDot11()
                .findBssidClientSignalStrengthHistogram(clientMac, timeRange, Bucketing.getConfig(timeRange), tapUUID)) {
            histogram.add(ClientSignalStrengthResponse.create(r.bucket(), r.signalStrength().longValue()));
        }

        return Response.ok(histogram).build();
    }

    @GET
    @Path("/show/{clientMac}/histogram/signal/disconnected")
    public Response disconnectedSignalStrengthHistogram(@Context SecurityContext sc,
                                                        @PathParam("clientMac") @NotEmpty String clientMac,
                                                        @QueryParam("time_range") @Valid String timeRangeParameter,
                                                        @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        if (tapUuids.size() != 1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        UUID tapUUID = tapUuids.get(0);

        Optional<ClientDetails> client = nzyme.getDot11().findMergedConnectedOrDisconnectedClient(
                clientMac, tapUuids, authenticatedUser
        );

        if (client.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<ClientSignalStrengthResponse> histogram = Lists.newArrayList();
        for (ClientSignalStrengthResult r : nzyme.getDot11().findDisconnectedClientSignalStrengthHistogram(
                clientMac, timeRange, Bucketing.getConfig(timeRange), tapUUID)) {
            histogram.add(ClientSignalStrengthResponse.create(
                    r.bucket(), r.signalStrength().longValue()
            ));
        }

        return Response.ok(histogram).build();
    }
}
