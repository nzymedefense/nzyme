package app.nzyme.core.rest.resources.uav;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.responses.uav.UavDetailsResponse;
import app.nzyme.core.rest.responses.uav.UavListResponse;
import app.nzyme.core.rest.responses.uav.enums.*;
import app.nzyme.core.uav.db.UavEntry;
import app.nzyme.core.util.TimeRange;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.UUID;

@Path("/api/uav")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class UavResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/tunnels")
    public Response findAll(@Context SecurityContext sc,
                            @QueryParam("time_range") @Valid String timeRangeParameter,
                            @QueryParam("limit") int limit,
                            @QueryParam("offset") int offset,
                            @QueryParam("taps") String tapIds) {
        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        long total = nzyme.getUav().countAllUavs(timeRange, taps);
        List<UavDetailsResponse> uavs = Lists.newArrayList();

        for (UavEntry uav : nzyme.getUav().findAllUavs(timeRange, limit, offset, taps)) {
            uavs.add(UavDetailsResponse.create(
                    uav.tapUuid(),
                    uav.identifier(),
                    UavTypeResponse.fromString(uav.uavType()),
                    UavDetectionSourceResponse.fromString(uav.detectionSource()),
                    uav.rssiAverage(),
                    UavOperationalStatusResponse.fromString(uav.operationalStatus()),
                    uav.latitude(),
                    uav.longitude(),
                    uav.groundTrack(),
                    uav.speed(),
                    uav.verticalSpeed(),
                    uav.altitudePressure(),
                    uav.altitudeGeodetic(),
                    UavHeightTypeResponse.fromString(uav.heightType()),
                    uav.height(),
                    uav.accuracyHorizontal(),
                    uav.accuracyVertical(),
                    uav.accuracyBarometer(),
                    uav.accuracySpeed(),
                    UavOperatorLocationTypeResponse.fromString(uav.operatorLocationType()),
                    uav.operatorLatitude(),
                    uav.operatorLongitude(),
                    uav.operatorAltitude(),
                    uav.firstSeen(),
                    uav.lastSeen()
            ));
        }

        return Response.ok(UavListResponse.create(total, uavs)).build();
    }

}
