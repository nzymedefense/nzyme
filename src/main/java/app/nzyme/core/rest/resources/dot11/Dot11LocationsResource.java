package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.TapBasedSignalStrengthResult;
import app.nzyme.core.dot11.trilateration.LocationSolver;
import app.nzyme.core.floorplans.db.TenantLocationEntry;
import app.nzyme.core.floorplans.db.TenantLocationFloorEntry;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.floorplans.*;
import app.nzyme.core.rest.responses.misc.ErrorResponse;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/dot11/locations")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class Dot11LocationsResource extends TapDataHandlingResource {

    private static final Logger LOG = LogManager.getLogger(Dot11LocationsResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/bssid/show/{bssid}/instant")
    public Response bssidInstantLocation(@Context SecurityContext sc,
                                         @PathParam("bssid") @NotEmpty String bssidParam,
                                         @QueryParam("minutes") int minutes,
                                         @QueryParam("taps") String tapsParam) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> tapUuids = parseAndValidateTapIds(authenticatedUser, nzyme, tapsParam);

        if (tapUuids.size() < 3) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.create("Number of selected taps insufficient for triangulation. " +
                            "Must be at least three.")).build();
        }

        List<Tap> taps = nzyme.getTapManager().findAllTapsByUUIDs(tapUuids);

        // Validate at least three taps passed, all placed at same tenant location.
        if (!validateTapsForTrilateration(taps)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.create("Tap selection not valid for triangulation. Must be at least " +
                            "three taps and all taps must be located at the same location. They do not have to " +
                            "be located on the same floor of the location.")).build();
        }

        List<TapBasedSignalStrengthResult> signalStrengths = nzyme.getDot11()
                .findBSSIDSignalStrengthPerTap(bssidParam, minutes, tapUuids);

        // Check that we have at least three taps here, too. It could be that one simply didn't record the BSSID.
        if (signalStrengths.size() < 3) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.create("A valid tap selection was made, but less than three taps " +
                            "recorded this BSSID.")).build();
        }

        // Determine tenant location and guess likely floor.
        TenantLocationEntry location = determineTenantLocation(taps);
        TenantLocationFloorEntry floor = nzyme.getTapManager().guessFloorOfSignalSource(location, signalStrengths);

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

        // Calculate location.
        LocationSolver solver = new LocationSolver(nzyme);
        LocationSolver.TrilaterationResult bssidLocation;
        try {
            bssidLocation = solver.solve(signalStrengths);
        } catch (LocationSolver.InvalidTapsException e) {
            LOG.error("Could not calculate BSSID location.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
                TrilaterationLocationResponse.create(bssidLocation.x(), bssidLocation.y()),
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
                TrilaterationDebugResponse.create(bssidLocation.tapDistances())
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
        /* We can grab the first tap, because the list of taps has been previously
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
