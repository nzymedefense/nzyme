package app.nzyme.core.rest.resources.gnss;


import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.generic.LatLonResult;
import app.nzyme.core.gnss.Constellation;
import app.nzyme.core.gnss.db.*;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.responses.gnss.*;
import app.nzyme.core.rest.responses.metrics.HistogramResponse;
import app.nzyme.core.rest.responses.shared.LatLonResponse;
import app.nzyme.core.rest.responses.taps.TapHighLevelInformationDetailsResponse;
import app.nzyme.core.shared.db.GenericIntegerHistogramEntry;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.Tools;
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
    @Path("/time/deviation/histogram")
    public Response timeDeviationHistogram(@Context SecurityContext sc,
                                           @QueryParam("time_range") @Valid String timeRangeParameter,
                                           @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);
        Bucketing.BucketingConfiguration bucketing = Bucketing.getConfig(timeRange);

        Map<DateTime, GNSSIntegerBucketResponse> histogram = Maps.newHashMap();
        for (GNSSIntegerBucket bucket : nzyme.getGnss().getTimeDeviationHistogram(timeRange, bucketing, taps)) {
            histogram.put(bucket.bucket(), GNSSIntegerBucketResponse.create(
                    bucket.gps(), bucket.glonass(), bucket.beidou(), bucket.galileo()
            ));
        }

        return Response.ok(histogram).build();
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
            satellites.add(SatelliteInViewResponse.create(
                    sat.constellation(),
                    sat.lastSeen(),
                    sat.prn(),
                    sat.snr(),
                    sat.azimuthDegrees(),
                    sat.elevationDegrees(),
                    sat.usedForFix()
            ));
        }

        return Response.ok(SatellitesInViewListResponse.create(satellites)).build();
    }

    @GET
    @Path("/constellations/{constellation}/prns/show/{prn}/snr/histogram")
    public Response constellationPrnSnrHistogram(@Context SecurityContext sc,
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
                .getPrnSnrHistogram(constellation, prn, timeRange, bucketing, taps)) {
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

}
