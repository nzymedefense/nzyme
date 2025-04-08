package app.nzyme.core.rest.resources.uav;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.geo.HaversineDistance;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.requests.CreateUavCustomTypeRequest;
import app.nzyme.core.rest.responses.shared.ClassificationResponse;
import app.nzyme.core.rest.responses.uav.*;
import app.nzyme.core.rest.responses.uav.enums.*;
import app.nzyme.core.rest.responses.uav.types.UavBuiltInTypeListResponse;
import app.nzyme.core.rest.responses.uav.types.UavCustomTypeDetailsResponse;
import app.nzyme.core.rest.responses.uav.types.UavCustomTypeListResponse;
import app.nzyme.core.shared.Classification;
import app.nzyme.core.uav.db.UavEntry;
import app.nzyme.core.uav.db.UavTimelineEntry;
import app.nzyme.core.uav.db.UavTypeEntry;
import app.nzyme.core.uav.types.UavTypeMatchType;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/uav")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class UavResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}")
    public Response findAll(@Context SecurityContext sc,
                            @QueryParam("time_range") @Valid String timeRangeParameter,
                            @QueryParam("limit") int limit,
                            @QueryParam("offset") int offset,
                            @PathParam("organization_id") UUID organizationId,
                            @PathParam("tenant_id") UUID tenantId,
                            @QueryParam("taps") String tapIds) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        long total = nzyme.getUav().countAllUavs(timeRange, taps);
        List<UavSummaryResponse> uavs = Lists.newArrayList();

        for (UavEntry uav : nzyme.getUav().findAllUavsOfTenant(timeRange, limit, offset, organizationId, tenantId, taps)) {
            uavs.add(uavEntryToSummaryResponse(uav));
        }

        return Response.ok(UavListResponse.create(total, uavs)).build();
    }

    @GET
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/show/{identifier}")
    public Response findOne(@Context SecurityContext sc,
                            @PathParam("identifier") String uavIdentifier,
                            @PathParam("organization_id") UUID organizationId,
                            @PathParam("tenant_id") UUID tenantId,
                            @QueryParam("taps") String tapIds) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        Optional<UavEntry> uav = nzyme.getUav().findUav(uavIdentifier, organizationId, tenantId, taps);

        if (uav.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(UavDetailsResponse.create(
                uavEntryToSummaryResponse(uav.get())
        )).build();
    }

    @GET
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/show/{identifier}/timelines")
    public Response findTimelines(@Context SecurityContext sc,
                                  @PathParam("identifier") String uavIdentifier,
                                  @PathParam("organization_id") UUID organizationId,
                                  @PathParam("tenant_id") UUID tenantId,
                                  @QueryParam("time_range") @Valid String timeRangeParameter,
                                  @QueryParam("limit") int limit,
                                  @QueryParam("offset") int offset,
                                  @QueryParam("taps") String tapIds) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<UUID> taps = parseAndValidateTapIds(getAuthenticatedUser(sc), nzyme, tapIds);

        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        long count = nzyme.getUav().countTimelines(uavIdentifier, timeRange, organizationId, tenantId, taps);
        List<UavTimelineDetailsResponse> timelines = Lists.newArrayList();
        for (UavTimelineEntry timeline : nzyme.getUav()
                .findUavTimelines(uavIdentifier, timeRange, organizationId, tenantId, taps, limit, offset)) {
            Duration duration = new Duration(timeline.seenFrom(), timeline.seenTo());

            timelines.add(UavTimelineDetailsResponse.create(
                    timeline.seenTo().isAfter(DateTime.now().minusMinutes(5)),
                    timeline.uuid(),
                    timeline.seenFrom(),
                    timeline.seenTo(),
                    duration.getStandardSeconds(),
                    Tools.durationToHumanReadable(duration)
            ));
        }

        return Response.ok(UavTimelineListResponse.create(count, timelines)).build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "uav_monitoring_manage" })
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/show/{identifier}/classify/{classification}")
    public Response classifyUav(@Context SecurityContext sc,
                                @PathParam("organization_id") UUID organizationId,
                                @PathParam("tenant_id") UUID tenantId,
                                @PathParam("identifier") String uavIdentifier,
                                @PathParam("classification") String c) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Update classification.
        Classification classification;
        try {
            classification = Classification.valueOf(c);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getUav().setUavClassification(uavIdentifier, organizationId, tenantId, classification);

        return Response.ok().build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY)
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/types/custom")
    public Response findAllCustomTypes(@Context SecurityContext sc,
                                       @PathParam("organization_id") UUID organizationId,
                                       @PathParam("tenant_id") UUID tenantId,
                                       @QueryParam("limit") int limit,
                                       @QueryParam("offset") int offset) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long count = nzyme.getUav().countAllCustomTypes(organizationId, tenantId);
        List<UavCustomTypeDetailsResponse> types = Lists.newArrayList();
        for (UavTypeEntry type : nzyme.getUav().findAllCustomTypes(organizationId, tenantId, limit, offset)) {
            types.add(UavCustomTypeDetailsResponse.create(
                    type.uuid(),
                    type.organizationId(),
                    type.tenantId(),
                    type.matchType(),
                    type.matchValue(),
                    type.defaultClassification(),
                    type.type(),
                    type.name(),
                    type.model(),
                    type.createdAt(),
                    type.updatedAt()
            ));
        }

        return Response.ok(UavCustomTypeListResponse.create(count, types)).build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "uav_monitoring_manage" })
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/types/custom")
    public Response createCustomType(@Context SecurityContext sc,
                                     @PathParam("organization_id") UUID organizationId,
                                     @PathParam("tenant_id") UUID tenantId,
                                     @Valid CreateUavCustomTypeRequest req) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        UavTypeMatchType matchType;
        Classification defaultClassification;
        try {
            matchType = UavTypeMatchType.valueOf(req.matchType().toUpperCase());

            if (req.defaultClassification() != null && !req.defaultClassification().isEmpty()) {
                defaultClassification = Classification.valueOf(req.defaultClassification().toUpperCase());
            } else {
                defaultClassification = null;
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getUav().createCustomType(
                organizationId,
                tenantId,
                matchType,
                req.matchValue(),
                defaultClassification,
                req.type(),
                req.name(),
                req.model() == null || req.model().trim().isEmpty() ? null : req.model()
        );

        return Response.status(Response.Status.CREATED).build();
    }


    @GET
    @RESTSecured(value = PermissionLevel.ANY)
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/types/builtin")
    public Response findAllBuiltInTypes(@Context SecurityContext sc,
                                        @PathParam("organization_id") UUID organizationId,
                                        @PathParam("tenant_id") UUID tenantId,
                                        @QueryParam("limit") int limit,
                                        @QueryParam("offset") int offset) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(UavBuiltInTypeListResponse.create(0, Lists.newArrayList())).build();
    }

    private UavSummaryResponse uavEntryToSummaryResponse(UavEntry uav) {
        Double operatorDistanceToUav = null;

        if (uav.latitude() != null && uav.longitude() != null
                && uav.operatorLatitude() != null && uav.operatorLongitude() != null) {
            operatorDistanceToUav = HaversineDistance.haversine(
                    uav.latitude(), uav.longitude(), uav.operatorLatitude(), uav.operatorLongitude()
            );
        }

        return UavSummaryResponse.create(
                uav.lastSeen().isAfter(DateTime.now().minusMinutes(5)),
                uav.identifier(),
                uav.designation(),
                ClassificationResponse.valueOf(uav.classification()),
                UavTypeResponse.fromString(uav.uavType()),
                UavDetectionSourceResponse.fromString(uav.detectionSource()),
                uav.idSerial(),
                uav.idRegistration(),
                uav.idUtm(),
                uav.idSession(),
                uav.operatorId(),
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
                operatorDistanceToUav,
                uav.latestVectorTimestamp(),
                uav.latestOperatorLocationTimestamp(),
                uav.firstSeen(),
                uav.lastSeen()
        );
    }

}
