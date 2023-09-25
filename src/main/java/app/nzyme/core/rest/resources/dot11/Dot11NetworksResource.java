package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.db.*;
import app.nzyme.core.dot11.db.monitoring.MonitoredSSID;
import app.nzyme.core.dot11.tracks.Track;
import app.nzyme.core.dot11.tracks.TrackDetector;
import app.nzyme.core.dot11.tracks.db.TrackDetectorConfig;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.UpdateTrackDetectorConfigurationRequest;
import app.nzyme.core.rest.responses.dot11.*;
import app.nzyme.core.taps.Tap;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Path("/api/dot11/networks")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class Dot11NetworksResource extends TapDataHandlingResource {

    private static final Logger LOG = LogManager.getLogger(Dot11NetworksResource.class);

    private final static List<Integer> DEFAULT_X_VALUES = Lists.newArrayList();

    static {
        for (int cnt = -100; cnt < 0; cnt++) {
            DEFAULT_X_VALUES.add(cnt);
        }
    }

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
                    nzyme.getOUIManager().lookupMac(bssid.bssid()),
                    bssid.securityProtocols(),
                    bssid.signalStrengthAverage(),
                    bssid.lastSeen(),
                    bssid.clientCount(),
                    bssid.fingerprints(),
                    bssid.ssids(),
                    bssid.hiddenSSIDFrames() > 0,
                    bssid.infrastructureTypes()
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

        List<SSIDChannelDetails> ssids = nzyme.getDot11().findSSIDsOfBSSID(minutes, bssid, tapUuids);

        // Find main active channel per SSID.
        Map<String, Map<Integer, Long>> activeChannels = Maps.newHashMap();
        for (SSIDChannelDetails ssid : ssids) {
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
            long highestCount = 0;
            for (Map.Entry<Integer, Long> count : channel.getValue().entrySet()) {
                if (count.getValue() > highestCount) {
                    highestCount = count.getValue();
                    mostActiveChannel = count.getKey();
                }
            }

            mostActiveChannels.put(ssid, mostActiveChannel);
        }

        List<SSIDChannelDetailsResponse> ssidsResult = Lists.newArrayList();
        for (SSIDChannelDetails ssid : ssids) {
            ssidsResult.add(SSIDChannelDetailsResponse.create(
                    ssid.ssid(),
                    ssid.frequency(),
                    Dot11.frequencyToChannel(ssid.frequency()),
                    ssid.signalStrengthAverage(),
                    ssid.totalFrames(),
                    ssid.totalBytes(),
                    mostActiveChannels.get(ssid.ssid()).equals(ssid.frequency()),
                    ssid.securityProtocols(),
                    ssid.infrastructureTypes(),
                    ssid.isWps(),
                    ssid.lastSeen()
            ));
        }

        return Response.ok(SSIDChannelListResponse.create(ssidsResult)).build();
    }

    @GET
    @Path("/bssids/histogram")
    public Response histogram(@Context SecurityContext sc,
                           @QueryParam("minutes") int minutes,
                           @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        Map<DateTime, BSSIDAndSSIDCountHistogramEntry> histogram = Maps.newHashMap();
        for (BSSIDAndSSIDCountHistogramEntry h : nzyme.getDot11().getBSSIDAndSSIDCountHistogram(minutes, tapUuids)) {
            histogram.put(h.bucket(), h);
        }

        Map<DateTime, BSSIDAndSSIDHistogramValueResponse> response = Maps.newTreeMap();
        for (int x = minutes; x != 0; x--) {
            DateTime bucket = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0).minusMinutes(x);
            BSSIDAndSSIDCountHistogramEntry entry = histogram.get(bucket);
            if (entry == null) {
                response.put(bucket,
                        BSSIDAndSSIDHistogramValueResponse.create(bucket, 0, 0)
                );
            } else {
                response.put(
                        bucket,
                        BSSIDAndSSIDHistogramValueResponse.create(
                                entry.bucket(),
                                entry.bssidCount(),
                                entry.ssidCount()
                        )
                );
            }
        }

        return Response.ok(BSSIDAndSSIDHistogramResponse.create(response)).build();
    }

    @GET
    @Path("/bssids/show/{bssid}/ssids/show/{ssid}")
    public Response ssidOfBSSID(@Context SecurityContext sc,
                                @PathParam("bssid") String bssid,
                                @PathParam("ssid") String ssid,
                                @QueryParam("minutes") int minutes,
                                @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        Optional<SSIDDetails> dbResult = nzyme.getDot11().findSSIDDetails(minutes, bssid, ssid, tapUuids);

        if (dbResult.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        SSIDDetails ssidDetails = dbResult.get();

        ObjectMapper om = new ObjectMapper();
        List<SecuritySuitesResponse> securitySuites = Lists.newArrayList();
        for (String suite : ssidDetails.securitySuites()) {
            try {
                Dot11SecuritySuiteJson info = om.readValue(suite, Dot11SecuritySuiteJson.class);
                securitySuites.add(SecuritySuitesResponse.create(
                        info.pairwiseCiphers(),
                        info.groupCipher(),
                        info.keyManagementModes(),
                        Dot11.securitySuitesToIdentifier(info)
                ));
            } catch (JsonProcessingException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        List<BSSIDClientDetails> accessPointClients = Lists.newArrayList();
        for (String mac : ssidDetails.accessPointClients()) {
            if (mac != null) {
                accessPointClients.add(BSSIDClientDetails.create(mac, nzyme.getOUIManager().lookupMac(mac)));
            }
        }

        SSIDDetailsResponse response = SSIDDetailsResponse.create(
                bssid,
                nzyme.getOUIManager().lookupMac(bssid),
                ssidDetails.ssid(),
                ssidDetails.frequencies(),
                ssidDetails.signalStrengthAverage(),
                ssidDetails.totalFrames(),
                ssidDetails.totalBytes(),
                ssidDetails.securityProtocols(),
                ssidDetails.fingerprints(),
                accessPointClients,
                ssidDetails.rates(),
                ssidDetails.infrastructureTypes(),
                securitySuites,
                ssidDetails.isWps(),
                ssidDetails.lastSeen()
        );

        return Response.ok(response).build();
    }

    @GET
    @Path("/bssids/show/{bssid}/ssids/show/{ssid}/advertisements/histogram")
    public Response ssidOfBSSIDAdvertisementHistogram(@Context SecurityContext sc,
                                                      @PathParam("bssid") String bssid,
                                                      @PathParam("ssid") String ssid,
                                                      @QueryParam("minutes") int minutes,
                                                      @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        Map<DateTime, SSIDAdvertisementHistogramEntry> histogram = Maps.newHashMap();
        for (SSIDAdvertisementHistogramEntry entry : nzyme.getDot11()
                .getSSIDAdvertisementHistogram(bssid, ssid, minutes, tapUuids)) {
            histogram.put(entry.bucket(), entry);
        }

        Map<DateTime, SSIDAdvertisementHistogramValueResponse> response = Maps.newTreeMap();
        for (int x = minutes; x != 0; x--) {
            DateTime bucket = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0).minusMinutes(x);
            SSIDAdvertisementHistogramEntry entry = histogram.get(bucket);
            if (entry == null) {
                response.put(bucket,
                        SSIDAdvertisementHistogramValueResponse.create(bucket, 0, 0)
                );
            } else {
                response.put(
                        bucket,
                        SSIDAdvertisementHistogramValueResponse.create(
                                entry.bucket(),
                                entry.beacons(),
                                entry.probeResponses()
                        )
                );
            }
        }

        return Response.ok(SSIDAdvertisementHistogramResponse.create(response)).build();
    }

    @GET
    @Path("/bssids/show/{bssid}/ssids/show/{ssid}/frequencies/histogram")
    public Response ssidOfBSSIDActiveChannelHistogram(@Context SecurityContext sc,
                                                      @PathParam("bssid") String bssid,
                                                      @PathParam("ssid") String ssid,
                                                      @QueryParam("minutes") int minutes,
                                                      @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        List<ActiveChannelDetailsResponse> channels = Lists.newArrayList();
        for (ActiveChannel c : nzyme.getDot11().getSSIDChannelUsageHistogram(bssid, ssid, minutes, tapUuids)) {
            channels.add(ActiveChannelDetailsResponse.create(
                    Dot11.frequencyToChannel(c.frequency()),
                    c.frequency(),
                    c.frames(),
                    c.bytes()
            ));
        }

        return Response.ok(ActiveChannelListResponse.create(channels)).build();
    }

    @GET
    @Path("/bssids/show/{bssid}/ssids/show/{ssid}/frequencies/show/{frequency}/signal/waterfall")
    public Response ssidOfBSSIDSignalWaterfall(@Context SecurityContext sc,
                                               @PathParam("bssid") String bssid,
                                               @PathParam("ssid") String ssid,
                                               @PathParam("frequency") int frequency,
                                               @QueryParam("minutes") int minutes,
                                               @QueryParam("taps") String taps) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, taps);

        if (tapUuids.size() != 1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Track detector config. Pull related org from tap (access was checked above and count is 1)
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Tap tap = nzyme.getTapManager().findTap(tapUuids.get(0)).get();
        TrackDetectorConfig config = nzyme.getDot11()
                .findCustomTrackDetectorConfiguration(tap.organizationId(), tap.uuid(), bssid, ssid, frequency)
                .orElse(TrackDetector.DEFAULT_CONFIG);

        List<ChannelHistogramEntry> signals = nzyme.getDot11().getSSIDSignalStrengthWaterfall(
                bssid, ssid, frequency, minutes, tap.uuid());

        TrackDetector.TrackDetectorHeatmapData heatmap = TrackDetector.toChartAxisMaps(signals);

        TrackDetector td = new TrackDetector();
        List<SignalWaterfallTrackResponse> tracks = Lists.newArrayList();
        for (Track track : td.detect(heatmap.z(), heatmap.y(), config)) {
            tracks.add(SignalWaterfallTrackResponse.create(
                    track.start(),
                    track.end(),
                    track.centerline(),
                    track.minSignal(),
                    track.maxSignal()
            ));
        }

        return Response.ok(
                SignalWaterfallResponse.create(
                        heatmap.z(),
                        DEFAULT_X_VALUES,
                        heatmap.y(),
                        tracks,
                        SignalWaterfallConfigurationResponse.create(
                                config.frameThreshold(),
                                TrackDetector.DEFAULT_CONFIG.frameThreshold(),
                                config.gapThreshold(),
                                TrackDetector.DEFAULT_CONFIG.gapThreshold(),
                                config.signalCenterlineJitter(),
                                TrackDetector.DEFAULT_CONFIG.signalCenterlineJitter()
                        )
                )
        ).build();
    }

    @PUT
    @Path("/bssids/show/{bssid}/ssids/show/{ssid}/frequencies/show/{frequency}/signal/trackdetector/configuration")
    public Response updateTrackDetectorConfig(@Context SecurityContext sc,
                                              @PathParam("bssid") String bssid,
                                              @PathParam("ssid") String ssid,
                                              @PathParam("frequency") int frequency,
                                              UpdateTrackDetectorConfigurationRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        // Check if user has access to this tap.
        if (!nzyme.getTapManager().allTapUUIDsAccessibleByUser(authenticatedUser).contains(req.tapId())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Known to exist because of permission check.
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Tap tap = nzyme.getTapManager().findTap(req.tapId()).get();

        LOG.info(req);

        nzyme.getDot11().updateCustomTrackDetectorConfiguration(
                tap.organizationId(),
                tap.uuid(),
                bssid,
                ssid,
                frequency,
                (int) req.frameThreshold(),
                (int)  req.gapThreshold(),
                (int) req.signalCenterlineJitter()
        );

        return Response.ok().build();
    }

    @GET
    @Path("/ssids/names")
    public Response allSSIDNamesFromAllAccessibleTaps(@Context SecurityContext sc) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, "*");

        return Response.ok(nzyme.getDot11().findAllSSIDNames(tapUuids)).build();
    }
}
