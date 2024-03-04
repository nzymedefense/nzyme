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
import app.nzyme.core.rest.responses.floorplans.*;
import app.nzyme.core.rest.responses.misc.ErrorResponse;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
                                         @QueryParam("minutes") int minutes) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        TenantLocationEntry location;
        TenantLocationFloorEntry floor;

        // Was a floor/location selected or do we guess?
        if (floorUuid == null || locationUuid == null) {
            // We have to guess.
            List<TapBasedSignalStrengthResult> instantSignalStrengths = nzyme.getDot11()
                    .findBSSIDSignalStrengthPerTap(
                            bssidParam,
                            minutes,
                            nzyme.getTapManager().allTapUUIDsAccessibleByUser(authenticatedUser)
                    );

            floor = nzyme.getTapManager().guessFloorOfSignalSource(instantSignalStrengths);
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

        // Make sure taps are valid.
        if (!validateTapsForTrilateration(taps)) {
            // This needs no user error message because the UI pre-selects only valid floors.
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<UUID> tapUuids = taps.stream().map(Tap::uuid).collect(Collectors.toList());

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
        List<TapBasedSignalStrengthResultHistogramEntry> signals = nzyme.getDot11()
                .getBSSIDSignalStrengthPerTapHistogram(bssidParam, minutes, tapUuids);

        // Calculate location.
        LocationSolver solver = new LocationSolver(nzyme);
        LocationSolver.TrilaterationResult bssidLocation;
        try {
            bssidLocation = solver.solve(signals, minutes);
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

    private boolean validateTapsForTrilateration(List<Tap> taps) {
        // All must be in same tenant location.
        UUID locationUUID = null;

        for (Tap tap : taps) {
            if (tap.locationId() == null) {
                LOG.debug("Passed taps insufficient for trilateration. Tap [{}/{}] not placed on any " +
                        "floor plan.", tap.name(), tap.uuid());
                return false;
            }

            if (locationUUID == null) {
                // First tap.
                locationUUID = tap.locationId();
                continue;
            }

            if (!tap.locationId().equals(locationUUID)) {
                LOG.debug("Passed taps insufficient for trilateration. Tap [{}/{}] not placed at " +
                        "same location [{}] as others.", tap.name(), tap.uuid(), locationUUID);
                return false;
            }
        }

        return true;
    }

    private TenantLocationEntry determineTenantLocation(List<Tap> taps) {
        /*
         * We can grab the first tap, because the list of taps has been previously
         * validated, and they all have to have the same location.
         */
        Tap tap = taps.get(0);

        Optional<TenantLocationEntry> result = nzyme.getAuthenticationService().findTenantLocation(
                tap.locationId(), tap.organizationId(), tap.tenantId()
        );

        if (result.isEmpty()) {
            // This should never happen because we validated tap list previously.
            throw new RuntimeException("No tenant location found.");
        }

        return result.get();
    }

}
