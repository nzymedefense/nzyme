package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.TapBasedSignalStrengthResult;
import app.nzyme.core.dot11.db.TapBasedSignalStrengthResultHistogramEntry;
import app.nzyme.core.dot11.trilateration.LocationSolver;
import app.nzyme.core.floorplans.db.TenantLocationEntry;
import app.nzyme.core.floorplans.db.TenantLocationFloorEntry;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.constraints.MacAddress;
import app.nzyme.core.rest.parameters.TimeRangeParameter;
import app.nzyme.core.rest.responses.floorplans.*;
import app.nzyme.core.rest.responses.misc.ErrorResponse;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Path("/api/dot11/locations")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class Dot11LocationsResource extends TapDataHandlingResource {

    private static final Logger LOG = LogManager.getLogger(Dot11LocationsResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/show/{locationId}/floors")
    public Response findAllFloorsOfLocation(@Context SecurityContext sc,
                                            @PathParam("locationId") UUID locationId,
                                            @QueryParam("limit") int limit,
                                            @QueryParam("offset") int offset) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        // Find location.
        Optional<TenantLocationEntry> location = nzyme.getAuthenticationService()
                .findTenantLocation(locationId, authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (location.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long floorCount = nzyme.getAuthenticationService().countAllFloorsOfTenantLocation(location.get().uuid());
        List<TenantLocationFloorDetailsResponse> floors = Lists.newArrayList();
        for (TenantLocationFloorEntry floor : nzyme.getAuthenticationService()
                .findAllFloorsOfTenantLocation(location.get().uuid(), limit, offset)) {
            List<TapPositionResponse> tapPositions = Lists.newArrayList();
            for (Tap t : nzyme.getTapManager()
                    .findAllTapsOnFloor(
                            authenticatedUser.getOrganizationId(),
                            authenticatedUser.getTenantId(),
                            locationId,
                            floor.uuid())) {
                //noinspection DataFlowIssue
                tapPositions.add(TapPositionResponse.create(
                        t.uuid(), t.name(), t.x(), t.y(), t.lastReport(), Tools.isTapActive(t.lastReport())
                ));
            }

            floors.add(TenantLocationFloorDetailsResponse.create(
                    floor.uuid(),
                    floor.locationId(),
                    floor.number(),
                    floor.name() == null ? "Floor " + floor.number() : floor.name(),
                    floor.plan() != null,
                    tapPositions.size(),
                    tapPositions,
                    floor.createdAt(),
                    floor.updatedAt()
            ));
        }

        return Response.ok(TenantLocationFloorListResponse.create(floorCount, floors)).build();
    }

    @GET
    @Path("/locate/bssid/show/{bssid}")
    public Response bssidInstantLocation(@Context SecurityContext sc,
                                         @MacAddress @PathParam("bssid") @NotEmpty String bssidParam,
                                         @QueryParam("floor_uuid") @Nullable UUID floorUuid,
                                         @QueryParam("location_uuid") @Nullable UUID locationUuid,
                                         @QueryParam("time_range") @Valid String timeRangeParameter) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        TenantLocationEntry location;
        TenantLocationFloorEntry floor;

        // Was a floor/location selected or do we guess?
        if (floorUuid == null || locationUuid == null) {
            // We have to guess.
            List<TapBasedSignalStrengthResult> instantSignalStrengths = nzyme.getDot11()
                    .findBSSIDSignalStrengthPerTap(
                            bssidParam,
                            timeRange,
                            nzyme.getTapManager().allTapUUIDsAccessibleByUser(authenticatedUser)
                    );

            if (instantSignalStrengths.size() < 3) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.create("Less than three taps recorded this " +
                                "BSSID during the selected timeframe and trilateration cannot be performed."))
                        .build();
            }

            Optional<TenantLocationFloorEntry> guessedFloor = nzyme.getTapManager()
                    .guessFloorOfSignalSource(instantSignalStrengths);

            if (guessedFloor.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.create("The system could not determine a floor " +
                                "with at least three placed taps that have recorded a signal of the BSSID during the " +
                                "selected timeframe. Trilateration cannot be performed."))
                        .build();
            }

            floor = guessedFloor.get();

            Optional<TenantLocationEntry> locationResult = nzyme.getAuthenticationService().findTenantLocation(
                    floor.locationId(), authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId()
            );

            if (locationResult.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            location = locationResult.get();
        } else {
            // Floor/location was passed.
            Optional<TenantLocationEntry> locationResult = nzyme.getAuthenticationService().findTenantLocation(
                    locationUuid, authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId()
            );

            if (locationResult.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            location = locationResult.get();

            Optional<TenantLocationFloorEntry> floorResult = nzyme.getAuthenticationService()
                    .findFloorOfTenantLocation(location.uuid(), floorUuid);

            if (floorResult.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            floor = floorResult.get();
        }

        // Get all taps of floor.
        List<Tap> taps = nzyme.getTapManager().findAllTapsOnFloor(
                authenticatedUser.getOrganizationId(),
                authenticatedUser.getTenantId(),
                location.uuid(),
                floor.uuid()
        );

        if (taps.size() < 3) {
            /*
             * Selected floor does not have at least three taps. We would not have guessed this one. Was manual
             * selection, which UI prevents.
             */
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.create("Selected floor does not have at least three placed taps."))
                    .build();
        }

        long locationFloorCount = nzyme.getAuthenticationService().countFloorsOfTenantLocation(location.uuid());
        long locationTapCount = nzyme.getAuthenticationService().countTapsOfTenantLocation(location.uuid());

        List<TapPositionResponse> tapPositions = Lists.newArrayList();
        for (Tap t : nzyme.getTapManager()
                .findAllTapsOnFloor(location.organizationId(), location.tenantId(), location.uuid(), floor.uuid())) {
            //noinspection DataFlowIssue
            tapPositions.add(TapPositionResponse.create(
                    t.uuid(), t.name(), t.x(), t.y(), t.lastReport(), Tools.isTapActive(t.lastReport())
            ));
        }

        // Get location heatmap data.
        List<UUID> tapUuids = taps.stream().map(Tap::uuid).collect(Collectors.toList());
        List<TapBasedSignalStrengthResultHistogramEntry> signals = nzyme.getDot11()
                .getBSSIDSignalStrengthPerTapHistogram(bssidParam, timeRange, tapUuids);

        if (!validateSignalsForTrilateration(signals)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.create("The determined/selected floor has three placed taps but less than " +
                            "three taps recorded this BSSID during the selected timeframe. Trilateration cannot be " +
                            "performed."))
                    .build();
        }

        // Calculate location.
        LocationSolver solver = new LocationSolver(nzyme);
        LocationSolver.TrilaterationResult bssidLocation;
        try {
            bssidLocation = solver.solve(signals);
        } catch (LocationSolver.InvalidTapsException e) {
            LOG.error("Could not calculate BSSID location.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        Map<DateTime, TrilaterationLocationResponse> locations = Maps.newTreeMap();
        for (Map.Entry<DateTime, LocationSolver.TrilaterationLocation> loc : bssidLocation.locations().entrySet()) {
            locations.put(loc.getKey(), TrilaterationLocationResponse.create(loc.getValue().x(), loc.getValue().y()));
        }

        // Get floor plan.
        BufferedImage floorPlanImage;
        try {
            if (floor.plan() == null) {
                LOG.info("Floor plan of floor [{}] is null. This is a database inconsistency.", floor.uuid());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            floorPlanImage = ImageIO.read(new ByteArrayInputStream(floor.plan()));
        } catch (Exception e) {
            LOG.error("Could not read floor plan image data from database. Floor: {}", floor, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok(TrilaterationResponse.create(
                locations,
                FloorPlanResponse.create(
                        BaseEncoding.base64().encode(floor.plan()),
                        floorPlanImage.getWidth(),
                        floorPlanImage.getHeight()
                ),
                TenantLocationDetailsResponse.create(
                        location.uuid(),
                        location.name(),
                        location.description(),
                        locationFloorCount,
                        locationTapCount,
                        location.createdAt(),
                        location.updatedAt()
                ),
                TenantLocationFloorDetailsResponse.create(
                        floor.uuid(),
                        floor.locationId(),
                        floor.number(),
                        floor.name(),
                        true, // It has if we reached here.
                        tapPositions.size(),
                        tapPositions,
                        floor.createdAt(),
                        floor.updatedAt()
                ),
                DateTime.now(),
                "BSSID " + bssidParam
        )).build();
    }

    private boolean validateSignalsForTrilateration(List<TapBasedSignalStrengthResultHistogramEntry> signals) {
        Map<DateTime, List<TapBasedSignalStrengthResultHistogramEntry>> histo = Maps.newHashMap();
        for (TapBasedSignalStrengthResultHistogramEntry signal : signals) {
            List<TapBasedSignalStrengthResultHistogramEntry> entry = histo.get(signal.bucket());

            if (entry != null) {
                entry.add(signal);
            } else {
                histo.put(signal.bucket(), new ArrayList<>(){{ add(signal); }});
            }
        }

        int validBuckets = 0;

        for (Map.Entry<DateTime, List<TapBasedSignalStrengthResultHistogramEntry>> entry : histo.entrySet()) {
            if (entry.getValue().size() >= 3) {
                validBuckets+=1;
            }
        }

        return validBuckets > 0;
    }

}
