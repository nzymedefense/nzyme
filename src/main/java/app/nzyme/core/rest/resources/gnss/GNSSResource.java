package app.nzyme.core.rest.resources.gnss;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.generic.LatLonResult;
import app.nzyme.core.gnss.Constellation;
import app.nzyme.core.gnss.GNSSRegistryKeys;
import app.nzyme.core.gnss.db.*;
import app.nzyme.core.gnss.db.elevationmasks.GNSSElevationMaskAzimuthBucket;
import app.nzyme.core.gnss.db.monitoring.GNSSMonitoringRuleEntry;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.CreateGNSSMonitoringRuleRequest;
import app.nzyme.core.rest.requests.GenericConfigurationUpdateRequest;
import app.nzyme.core.rest.requests.UpdateGNSSMonitoringRuleRequest;
import app.nzyme.core.rest.responses.gnss.*;
import app.nzyme.core.rest.responses.gnss.monitoring.GNSSMonitoringConfigurationResponse;
import app.nzyme.core.rest.responses.gnss.monitoring.GNSSMonitoringRuleDetailsResponse;
import app.nzyme.core.rest.responses.gnss.monitoring.GNSSMonitoringRulesListResponse;
import app.nzyme.core.rest.responses.shared.LatLonResponse;
import app.nzyme.core.rest.responses.taps.TapHighLevelInformationDetailsResponse;
import app.nzyme.core.shared.db.GenericIntegerHistogramEntry;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraintValidator;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryValueType;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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

@Path("/api/gnss")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class GNSSResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/constellations/{constellation}/coordinates")
    public Response constellationCoordinates(@Context SecurityContext sc,
                                             @PathParam("constellation") String constellationParam,
                                             @QueryParam("time_range") @Valid String timeRangeParameter,
                                             @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        Constellation constellation;
        try {
            constellation = Constellation.valueOf(constellationParam);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Coordinates.
        List<LatLonResponse> coordinates = Lists.newArrayList();
        for (LatLonResult coords : nzyme.getGnss().getRecordedCoordinates(constellation, timeRange, taps)) {
            coordinates.add(LatLonResponse.create(coords.lat(), coords.lon()));
        }

        // Tap locations.
        List<GNSSTapLocationResponse> tapLocations = Lists.newArrayList();
        for (UUID tapUuid : taps) {
            Optional<Tap> tap = nzyme.getTapManager().findTap(tapUuid);
            if (tap.isPresent() && tap.get().latitude() != null && tap.get().longitude() != null
                    && tap.get().latitude() != 0 && tap.get().longitude() != 0) {
                tapLocations.add(GNSSTapLocationResponse.create(
                        tap.get().latitude(),
                        tap.get().longitude(),
                        Tools.isTapActive(tap.get().lastReport()),
                        tap.get().name()
                ));
            }
        }

        return Response.ok(GNSSConstellationCoordinatesResponse.create(coordinates, tapLocations)).build();
    }

    @GET
    @Path("/pdop/histogram")
    public Response pdopHistogram(@Context SecurityContext sc,
                                  @QueryParam("time_range") @Valid String timeRangeParameter,
                                  @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, GNSSDoubleBucketResponse> histogram = Maps.newHashMap();
        for (GNSSDoubleBucket bucket : nzyme.getGnss().getPdopHistogram(timeRange, bucketing, taps)) {
            histogram.put(bucket.bucket(), GNSSDoubleBucketResponse.create(
                    bucket.gps(), bucket.glonass(), bucket.beidou(), bucket.galileo()
            ));
        }

        return Response.ok(histogram).build();
    }

    @GET
    @Path("/distances")
    public Response distances(@Context SecurityContext sc,
                              @QueryParam("time_range") @Valid String timeRangeParameter,
                              @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        List<GNSSConstellationDistancesResponse> response = Lists.newArrayList();

        for (UUID tapId : taps) {
            Optional<Tap> tap = nzyme.getTapManager().findTap(tapId);

            if (tap.isEmpty() || tap.get().latitude() == null || tap.get().longitude() == null) {
                continue;
            }

            GNSSConstellationDistances d = nzyme.getGnss().getConstellationDistancesFromTap(timeRange, tap.get());

            response.add(GNSSConstellationDistancesResponse.create(TapHighLevelInformationDetailsResponse.create(
                    tap.get().uuid(),
                    tap.get().name(),
                    Tools.isTapActive(tap.get().lastReport())
            ), d.gps(), d.glonass(), d.beidou(), d.galileo()));
        }

        return Response.ok(response).build();
    }

    @GET
    @Path("/fix/satellites/histogram")
    public Response fixSatellitesHistogram(@Context SecurityContext sc,
                                           @QueryParam("time_range") @Valid String timeRangeParameter,
                                           @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, GNSSIntegerBucketResponse> histogram = Maps.newHashMap();
        for (GNSSIntegerBucket bucket : nzyme.getGnss().getFixSatelliteHistogram(timeRange, bucketing, taps)) {
            histogram.put(bucket.bucket(), GNSSIntegerBucketResponse.create(
                    bucket.gps(), bucket.glonass(), bucket.beidou(), bucket.galileo()
            ));
        }

        return Response.ok(histogram).build();
    }

    @GET
    @Path("/fix/status/histogram")
    public Response fixStatusHistogram(@Context SecurityContext sc,
                                       @QueryParam("time_range") @Valid String timeRangeParameter,
                                       @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, GNSSStringBucketResponse> histogram = Maps.newHashMap();
        for (GNSSStringBucket bucket : nzyme.getGnss().getFixStatusHistogram(timeRange, bucketing, taps)) {
            histogram.put(bucket.bucket(), GNSSStringBucketResponse.create(
                    bucket.gps(), bucket.glonass(), bucket.beidou(), bucket.galileo()
            ));
        }

        return Response.ok(histogram).build();
    }

    @GET
    @Path("/altitude/histogram")
    public Response altitudeHistogram(@Context SecurityContext sc,
                                      @QueryParam("time_range") @Valid String timeRangeParameter,
                                      @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, GNSSIntegerBucketResponse> histogram = Maps.newHashMap();
        for (GNSSIntegerBucket bucket : nzyme.getGnss().getAltitudeHistogram(timeRange, bucketing, taps)) {
            histogram.put(bucket.bucket(), GNSSIntegerBucketResponse.create(
                    bucket.gps(), bucket.glonass(), bucket.beidou(), bucket.galileo()
            ));
        }

        return Response.ok(histogram).build();
    }

    @GET
    @Path("/satellites/visible/histogram")
    public Response satellitesInViewHistogram(@Context SecurityContext sc,
                                              @QueryParam("time_range") @Valid String timeRangeParameter,
                                              @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, GNSSIntegerBucketResponse> histogram = Maps.newHashMap();
        for (GNSSIntegerBucket bucket : nzyme.getGnss().getSatellitesInViewHistogram(timeRange, bucketing, taps)) {
            histogram.put(bucket.bucket(), GNSSIntegerBucketResponse.create(
                    bucket.gps(), bucket.glonass(), bucket.beidou(), bucket.galileo()
            ));
        }

        return Response.ok(histogram).build();
    }

    @GET
    @Path("/satellites/visible/list")
    public Response satellitesInViewList(@Context SecurityContext sc,
                                         @QueryParam("time_range") @Valid String timeRangeParameter,
                                         @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        List<SatelliteInViewResponse> satellites = Lists.newArrayList();
        for (GNSSSatelliteInView sat : nzyme.getGnss().findAllSatellitesInView(timeRange, taps)) {
            // Get previous positions to draw a track.
            Constellation constellation = Constellation.valueOf(sat.constellation());

            List<GNSSPRNTrackPointResponse> trackPoints = Lists.newArrayList();
            for (GNSSPRNTrackPoint point : nzyme.getGnss()
                    .findRecentSatelliteTrack(constellation, sat.prn(), DateTime.now().minusMinutes(15), taps)) {
                trackPoints.add(GNSSPRNTrackPointResponse.create(
                        point.averageSno(),
                        point.azimuthDegrees(),
                        point.elevationDegrees(),
                        point.timestamp()
                ));
            }

            satellites.add(SatelliteInViewResponse.create(
                    sat.constellation(),
                    sat.lastSeen(),
                    sat.prn(),
                    sat.averageSno(),
                    sat.azimuthDegrees(),
                    sat.elevationDegrees(),
                    sat.usedForFix(),
                    sat.averageDopplerHz(),
                    sat.maximumMultipathIndicator(),
                    sat.averagePseudorangeRmsError(),
                    trackPoints
            ));
        }

        return Response.ok(SatellitesInViewListResponse.create(satellites)).build();
    }

    @GET
    @Path("/elevationmask")
    public Response elevationMask(@Context SecurityContext sc,
                                  @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        Map<Integer, GNSSElevationMaskAzimuthBucketResponse> response = Maps.newHashMap();
        for (GNSSElevationMaskAzimuthBucket bucket : nzyme.getGnss().getElevationMask(taps)) {
            response.put(bucket.azimuthBucket(),  GNSSElevationMaskAzimuthBucketResponse.create(
                    bucket.azimuthBucket(),
                    bucket.skylineElevation(),
                    bucket.skylineElevationBestEffort(),
                    bucket.lowSubsetCount(),
                    bucket.minElevationObserved(),
                    bucket.usedFallback(),
                    bucket.snoMedian(),
                    bucket.snoP10(),
                    bucket.sampleCount(),
                    bucket.windowStart(),
                    bucket.windowEnd()
            ));
        }

        return Response.ok(response).build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "gnss_monitoring_manage" })
    @Path("/elevationmask/tap/{tapUuid}")
    public Response cleanElevationMask(@Context SecurityContext sc, @PathParam("tapUuid") UUID tapUuid) {
        AuthenticatedUser user = getAuthenticatedUser(sc);

        List<UUID> userAccessibleTaps = nzyme.getTapManager().allTapUUIDsAccessibleByUser(user);
        if (!userAccessibleTaps.contains(tapUuid)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        nzyme.getGnss().cleanElevationMask(tapUuid);

        return Response.ok().build();
    }

    @GET
    @Path("/rfmon/jamming-indicator/histogram")
    public Response rfmonJammingIndicatorHistogram(@Context SecurityContext sc,
                                                   @QueryParam("time_range") @Valid String timeRangeParameter,
                                                   @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, GNSSIntegerBucketResponse> histogram = Maps.newHashMap();
        for (GNSSIntegerBucket bucket : nzyme.getGnss().getMonRfJammingIndicatorHistogram(timeRange, bucketing, taps)) {
            histogram.put(bucket.bucket(), GNSSIntegerBucketResponse.create(
                    bucket.gps(), bucket.glonass(), bucket.beidou(), bucket.galileo()
            ));
        }

        return Response.ok(histogram).build();
    }

    @GET
    @Path("/rfmon/agc-count/histogram")
    public Response rfmonAgcCountHistogram(@Context SecurityContext sc,
                                           @QueryParam("time_range") @Valid String timeRangeParameter,
                                           @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, GNSSIntegerBucketResponse> histogram = Maps.newHashMap();
        for (GNSSIntegerBucket bucket : nzyme.getGnss().getMonRfAgcCountHistogram(timeRange, bucketing, taps)) {
            histogram.put(bucket.bucket(), GNSSIntegerBucketResponse.create(
                    bucket.gps(), bucket.glonass(), bucket.beidou(), bucket.galileo()
            ));
        }

        return Response.ok(histogram).build();
    }

    @GET
    @Path("/rfmon/noise/histogram")
    public Response rfmonNoiseHistogram(@Context SecurityContext sc,
                                        @QueryParam("time_range") @Valid String timeRangeParameter,
                                        @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, GNSSIntegerBucketResponse> histogram = Maps.newHashMap();
        for (GNSSIntegerBucket bucket : nzyme.getGnss().getMonRfNoiseHistogram(timeRange, bucketing, taps)) {
            histogram.put(bucket.bucket(), GNSSIntegerBucketResponse.create(
                    bucket.gps(), bucket.glonass(), bucket.beidou(), bucket.galileo()
            ));
        }

        return Response.ok(histogram).build();
    }

    @GET
    @Path("/constellations/{constellation}/prns/show/{prn}/sno/histogram")
    public Response constellationPrnSnoHistogram(@Context SecurityContext sc,
                                                 @PathParam("constellation") String constellationParam,
                                                 @PathParam("prn") @Min(1) int prn,
                                                 @QueryParam("time_range") @Valid String timeRangeParameter,
                                                 @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.BucketingConfiguration.create(Bucketing.Type.MINUTE);

        Constellation constellation;
        try {
            constellation = Constellation.valueOf(constellationParam);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Map<DateTime, Integer> response = Maps.newHashMap();
        for (GenericIntegerHistogramEntry bucket : nzyme.getGnss()
                .getPrnSnoHistogram(constellation, prn, timeRange, bucketing, taps)) {
            response.put(bucket.bucket(), bucket.value());
        }

        return Response.ok(response).build();
    }

    @GET
    @Path("/constellations/{constellation}/prns/show/{prn}/elevation/histogram")
    public Response constellationPrnElevationHistogram(@Context SecurityContext sc,
                                                       @PathParam("constellation") String constellationParam,
                                                       @PathParam("prn") @Min(1) int prn,
                                                       @QueryParam("time_range") @Valid String timeRangeParameter,
                                                       @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.BucketingConfiguration.create(Bucketing.Type.MINUTE);

        Constellation constellation;
        try {
            constellation = Constellation.valueOf(constellationParam);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Map<DateTime, Integer> response = Maps.newHashMap();

        for (GenericIntegerHistogramEntry bucket : nzyme.getGnss()
                .getPrnElevationHistogram(constellation, prn, timeRange, bucketing, taps)) {
            response.put(bucket.bucket(), bucket.value());
        }

        return Response.ok(response).build();
    }

    @GET
    @Path("/constellations/{constellation}/prns/show/{prn}/azimuth/histogram")
    public Response constellationPrnAzimuthHistogram(@Context SecurityContext sc,
                                                     @PathParam("constellation") String constellationParam,
                                                     @PathParam("prn") @Min(1) int prn,
                                                     @QueryParam("time_range") @Valid String timeRangeParameter,
                                                     @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.BucketingConfiguration.create(Bucketing.Type.MINUTE);

        Constellation constellation;
        try {
            constellation = Constellation.valueOf(constellationParam);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Map<DateTime, Integer> response = Maps.newHashMap();

        for (GenericIntegerHistogramEntry bucket : nzyme.getGnss()
                .getPrnAzimuthHistogram(constellation, prn, timeRange, bucketing, taps)) {
            response.put(bucket.bucket(), bucket.value());
        }

        return Response.ok(response).build();
    }

    @GET
    @Path("/constellations/{constellation}/prns/show/{prn}/doppler/histogram")
    public Response constellationPrnDopplerHistogram(@Context SecurityContext sc,
                                                     @PathParam("constellation") String constellationParam,
                                                     @PathParam("prn") @Min(1) int prn,
                                                     @QueryParam("time_range") @Valid String timeRangeParameter,
                                                     @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.BucketingConfiguration.create(Bucketing.Type.MINUTE);

        Constellation constellation;
        try {
            constellation = Constellation.valueOf(constellationParam);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Map<DateTime, Integer> response = Maps.newHashMap();

        for (GenericIntegerHistogramEntry bucket : nzyme.getGnss()
                .getPrnDopplerHistogram(constellation, prn, timeRange, bucketing, taps)) {
            response.put(bucket.bucket(), bucket.value());
        }

        return Response.ok(response).build();
    }

    @GET
    @Path("/constellations/{constellation}/prns/show/{prn}/multipath/histogram")
    public Response constellationPrnMultipathIndexHistogram(@Context SecurityContext sc,
                                                            @PathParam("constellation") String constellationParam,
                                                            @PathParam("prn") @Min(1) int prn,
                                                            @QueryParam("time_range") @Valid String timeRangeParameter,
                                                            @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.BucketingConfiguration.create(Bucketing.Type.MINUTE);

        Constellation constellation;
        try {
            constellation = Constellation.valueOf(constellationParam);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Map<DateTime, Integer> response = Maps.newHashMap();
        for (GenericIntegerHistogramEntry bucket : nzyme.getGnss()
                .getPrnMultipathIndexHistogram(constellation, prn, timeRange, bucketing, taps)) {
            response.put(bucket.bucket(), bucket.value());
        }

        return Response.ok(response).build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "gnss_monitoring_manage" })
    @Path("/monitoring/organization/{organizationId}/tenant/{tenantId}/rules/show/{uuid}")
    public Response findMonitoringRule(@Context SecurityContext sc,
                                       @PathParam("uuid") UUID uuid,
                                       @PathParam("organizationId") UUID organizationId,
                                       @PathParam("tenantId") UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<GNSSMonitoringRuleEntry> rule = nzyme.getGnss()
                .findMonitoringRuleOfTenant(uuid, organizationId, tenantId);

        if (rule.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(buildMonitoringRuleResponse(rule.get())).build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "gnss_monitoring_manage" })
    @Path("/monitoring/organization/{organizationId}/tenant/{tenantId}/rules")
    public Response findAllMonitoringRules(@Context SecurityContext sc,
                                           @PathParam("organizationId") UUID organizationId,
                                           @PathParam("tenantId") UUID tenantId,
                                           @QueryParam("limit") int limit,
                                           @QueryParam("offset") int offset) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        long total = nzyme.getGnss().countAllMonitoringRulesOfTenant(organizationId, tenantId);

        List<GNSSMonitoringRuleDetailsResponse> rules = Lists.newArrayList();
        for (GNSSMonitoringRuleEntry rule : nzyme.getGnss()
                .findAllMonitoringRulesOfTenant(organizationId, tenantId, limit, offset)) {
            rules.add(buildMonitoringRuleResponse(rule));
        }

        return Response.ok(GNSSMonitoringRulesListResponse.create(total, rules)).build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "gnss_monitoring_manage" })
    @Path("/monitoring/organization/{organizationId}/tenant/{tenantId}/rules")
    public Response createMonitoringRule(@Context SecurityContext sc,
                                         @PathParam("organizationId") UUID organizationId,
                                         @PathParam("tenantId") UUID tenantId,
                                         @Valid CreateGNSSMonitoringRuleRequest request) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, request.taps());

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        nzyme.getGnss().createMonitoringRule(
                request.name(), request.description(), request.conditions(), taps, organizationId, tenantId
        );

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "gnss_monitoring_manage" })
    @Path("/monitoring/organization/{organizationId}/tenant/{tenantId}/rules/show/{uuid}")
    public Response editMonitoringRule(@Context SecurityContext sc,
                                       @PathParam("uuid") UUID uuid,
                                       @PathParam("organizationId") UUID organizationId,
                                       @PathParam("tenantId") UUID tenantId,
                                       @Valid UpdateGNSSMonitoringRuleRequest request) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, request.taps());

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<GNSSMonitoringRuleEntry> rule = nzyme.getGnss()
                .findMonitoringRuleOfTenant(uuid, organizationId, tenantId);

        if (rule.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getGnss().updateMonitoringRule(
                rule.get().id(), request.name(), request.description(), request.conditions(), taps
        );

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "gnss_monitoring_manage" })
    @Path("/monitoring/organization/{organizationId}/tenant/{tenantId}/rules/show/{uuid}")
    public Response deleteMonitoringRule(@Context SecurityContext sc,
                                         @PathParam("uuid") UUID uuid,
                                         @PathParam("organizationId") UUID organizationId,
                                         @PathParam("tenantId") UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<GNSSMonitoringRuleEntry> rule = nzyme.getGnss()
                .findMonitoringRuleOfTenant(uuid, organizationId, tenantId);

        if (rule.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getGnss().deleteMonitoringRule(rule.get().id());

        return Response.ok().build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "gnss_monitoring_manage" })
    @Path("/monitoring/organization/{organizationId}/tenant/{tenantId}/configuration")
    public Response getMonitoringConfiguration(@Context SecurityContext sc,
                                               @PathParam("organizationId") UUID organizationId,
                                               @PathParam("tenantId") UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Integer trainingPeriodMinutes = nzyme.getDatabaseCoreRegistry()
                .getValue(GNSSRegistryKeys.GNSS_MONITORING_TRAINING_PERIOD_MINUTES.key(), organizationId, tenantId)
                .map(Integer::parseInt)
                .orElse(Integer.parseInt(GNSSRegistryKeys.GNSS_MONITORING_TRAINING_PERIOD_MINUTES.defaultValue().get()));

        GNSSMonitoringConfigurationResponse response = GNSSMonitoringConfigurationResponse.create(
                ConfigurationEntryResponse.create(
                        GNSSRegistryKeys.GNSS_MONITORING_TRAINING_PERIOD_MINUTES.key(),
                        "Training Period (minutes)",
                        trainingPeriodMinutes,
                        ConfigurationEntryValueType.NUMBER,
                        GNSSRegistryKeys.GNSS_MONITORING_TRAINING_PERIOD_MINUTES.defaultValue().orElse(null),
                        GNSSRegistryKeys.GNSS_MONITORING_TRAINING_PERIOD_MINUTES.requiresRestart(),
                        GNSSRegistryKeys.GNSS_MONITORING_TRAINING_PERIOD_MINUTES.constraints().orElse(null),
                        "gnss-monitoring-training-period"
                )
        );

        return Response.ok(response).build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "gnss_monitoring_manage" })
    @Path("/monitoring/organization/{organizationId}/tenant/{tenantId}/configuration")
    public Response updateMonitoringConfiguration(@Context SecurityContext sc,
                                                  @PathParam("organizationId") UUID organizationId,
                                                  @PathParam("tenantId") UUID tenantId,
                                                  @Valid GenericConfigurationUpdateRequest req) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (req.change().isEmpty()) {
            return Response.status(422).build();
        }

        for (Map.Entry<String, Object> c : req.change().entrySet()) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (c.getKey()) {
                case "gnss_monitoring_training_period_minutes":
                    if (!ConfigurationEntryConstraintValidator
                            .checkConstraints(GNSSRegistryKeys.GNSS_MONITORING_TRAINING_PERIOD_MINUTES, c)) {
                        return Response.status(422).build();
                    }
                    break;
                default:
                    return Response.status(Response.Status.BAD_REQUEST).build();
            }

            nzyme.getDatabaseCoreRegistry().setValue(c.getKey(), c.getValue().toString(), organizationId, tenantId);
        }

        return Response.ok().build();
    }

    private GNSSMonitoringRuleDetailsResponse buildMonitoringRuleResponse(GNSSMonitoringRuleEntry rule) {
        List<TapHighLevelInformationDetailsResponse> taps;

        if (rule.taps().isEmpty()) {
            taps = null;
        } else {
            List<TapHighLevelInformationDetailsResponse> tapInfos = Lists.newArrayList();

            for (UUID tapUuid : rule.taps().get()) {
                Optional<Tap> tap = nzyme.getTapManager().findTap(tapUuid);

                if (tap.isPresent()) {
                    tapInfos.add(TapHighLevelInformationDetailsResponse.create(
                            tap.get().uuid(),
                            tap.get().name(),
                            Tools.isTapActive(tap.get().lastReport())
                    ));
                }
            }

            taps = tapInfos;
        }

        return GNSSMonitoringRuleDetailsResponse.create(
                rule.uuid(),
                rule.organizationId(),
                rule.tenantId(),
                rule.name(),
                rule.description(),
                rule.conditions()
                        .values()
                        .stream()
                        .mapToInt(List::size)
                        .sum(),
                rule.conditions(),
                taps,
                rule.updatedAt(),
                rule.createdAt()
        );
    }

}
