package app.nzyme.core.rest.resources.gnss;


import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.generic.LatLonResult;
import app.nzyme.core.gnss.Constellation;
import app.nzyme.core.gnss.db.GNSSDoubleBucket;
import app.nzyme.core.gnss.db.GNSSIntegerBucket;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.responses.gnss.GNSSConstellationCoordinatesResponse;
import app.nzyme.core.rest.responses.gnss.GNSSDoubleBucketResponse;
import app.nzyme.core.rest.responses.gnss.GNSSIntegerBucketResponse;
import app.nzyme.core.rest.responses.gnss.GNSSTapLocationResponse;
import app.nzyme.core.rest.responses.shared.LatLonResponse;
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
    @Path("/satellites/visibie/histogram")
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


}
