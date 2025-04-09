package app.nzyme.core.rest.resources.uav;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.geo.HaversineDistance;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.requests.CreateUavCustomTypeRequest;
import app.nzyme.core.rest.requests.UavMonitoringConfigurationRequest;
import app.nzyme.core.rest.requests.UpdateUavCustomTypeRequest;
import app.nzyme.core.rest.responses.shared.ClassificationResponse;
import app.nzyme.core.rest.responses.uav.*;
import app.nzyme.core.rest.responses.uav.enums.*;
import app.nzyme.core.rest.responses.uav.monitoring.UavMonitoringSettingsResponse;
import app.nzyme.core.rest.responses.uav.types.UavConnectTypeDetailsResponse;
import app.nzyme.core.rest.responses.uav.types.UavConnectTypeListResponse;
import app.nzyme.core.rest.responses.uav.types.UavCustomTypeDetailsResponse;
import app.nzyme.core.rest.responses.uav.types.UavCustomTypeListResponse;
import app.nzyme.core.shared.Classification;
import app.nzyme.core.uav.UavRegistryKeys;
import app.nzyme.core.uav.db.UavEntry;
import app.nzyme.core.uav.db.UavTimelineEntry;
import app.nzyme.core.uav.db.UavTypeEntry;
import app.nzyme.core.uav.db.UavVectorEntry;
import app.nzyme.core.uav.types.ConnectUavModel;
import app.nzyme.core.uav.types.UavTypeMatch;
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

        List<UavTypeEntry> customTypes = nzyme.getUav().findAllCustomTypes(organizationId, tenantId);
        for (UavEntry uav : nzyme.getUav().findAllUavsOfTenant(timeRange, limit, offset, organizationId, tenantId, taps)) {
            uavs.add(uavEntryToSummaryResponse(uav, nzyme.getUav().matchUavType(customTypes, uav.idSerial())));
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

        Optional<UavTypeMatch> uavType = nzyme.getUav().matchUavType(uav.get().idSerial(), tenantId, organizationId);

        return Response.ok(UavDetailsResponse.create(
                uavEntryToSummaryResponse(uav.get(), uavType)
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

    @GET
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/show/{identifier}/timelines/show/{timeline_id}")
    public Response getTimelineVectors(@Context SecurityContext sc,
                                       @PathParam("identifier") String uavIdentifier,
                                       @PathParam("timeline_id") UUID timelineId,
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

        // Get Timeline
        Optional<UavTimelineEntry> timeline = nzyme.getUav()
                .findUavTimeline(uavIdentifier, timelineId, organizationId, tenantId, taps);

        // Does the timeline exist?
        if (timeline.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Query vectors for UAV during timeline duration.
        List<UavVectorDetailsResponse> vectors =  Lists.newArrayList();
        for (UavVectorEntry v : nzyme.getUav()
                .findVectorsOfTimeline(uav.get().id(), timeline.get().seenFrom(), timeline.get().seenTo())) {
            if (v.latitude() == null || v.longitude() == null) {
                continue;
            }

            vectors.add(UavVectorDetailsResponse.create(
                    v.timestamp(),
                    v.latitude(),
                    v.longitude(),
                    v.operationalStatus(),
                    v.groundTrack(),
                    v.speed(),
                    v.verticalSpeed(),
                    v.altitudePressure(),
                    v.altitudeGeodetic(),
                    v.heightType(),
                    v.height(),
                    v.accuracyHorizontal(),
                    v.accuracyVertical(),
                    v.accuracyBarometer(),
                    v.accuracySpeed()
            ));
        }

        return Response.ok(UavVectorListResponse.create(vectors.size(), vectors)).build();
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

    @GET
    @RESTSecured(value = PermissionLevel.ANY)
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/types/connect")
    public Response findAllConnectTypes(@QueryParam("limit") int limit,
                                        @QueryParam("offset") int offset) {
        Optional<Integer> count = nzyme.getUav().countAllConnectUavModels();

        if (count.isEmpty()) {
            return Response.ok(UavConnectTypeListResponse.create(0, Lists.newArrayList())).build();
        }

        List<UavConnectTypeDetailsResponse> types = Lists.newArrayList();

        Optional<List<ConnectUavModel>> models = nzyme.getUav().findAllConnectUavModels(limit, offset);

        if (models.isEmpty()) {
            return Response.ok(UavConnectTypeListResponse.create(0, Lists.newArrayList())).build();
        }

        for (ConnectUavModel model : models.get()) {
            types.add(UavConnectTypeDetailsResponse.create(
                    model.classification() == null ? "Unknown" : model.classification(),
                    model.make() + " " + model.model(),
                    model.serialType().toString(),
                    model.serial()
            ));
        }

        return Response.ok(UavConnectTypeListResponse.create(count.get(), types)).build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY)
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/types/custom/show/{uuid}")
    public Response findCustomType(@Context SecurityContext sc,
                                   @PathParam("uuid") UUID uuid,
                                   @PathParam("organization_id") UUID organizationId,
                                   @PathParam("tenant_id") UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<UavTypeEntry> result = nzyme.getUav().findCustomType(uuid, organizationId, tenantId);

        if (result.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        UavTypeEntry type = result.get();

        return Response.ok(UavCustomTypeDetailsResponse.create(
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
        )).build();
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

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "uav_monitoring_manage" })
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/types/custom/show/{uuid}")
    public Response updateCustomType(@Context SecurityContext sc,
                                     @PathParam("uuid") UUID uuid,
                                     @PathParam("organization_id") UUID organizationId,
                                     @PathParam("tenant_id") UUID tenantId,
                                     @Valid UpdateUavCustomTypeRequest req) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<UavTypeEntry> type = nzyme.getUav().findCustomType(uuid, organizationId, tenantId);

        if (type.isEmpty()) {
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


        nzyme.getUav().updateCustomType(
                type.get().id(),
                matchType,
                req.matchValue(),
                defaultClassification,
                req.type(),
                req.name(),
                req.model() == null || req.model().trim().isEmpty() ? null : req.model()
        );

        return Response.ok().build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "uav_monitoring_manage" })
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/types/custom/show/{uuid}")
    public Response deleteCustomType(@Context SecurityContext sc,
                                     @PathParam("organization_id") UUID organizationId,
                                     @PathParam("tenant_id") UUID tenantId,
                                     @PathParam("uuid") UUID uuid) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<UavTypeEntry> type = nzyme.getUav().findCustomType(uuid, organizationId, tenantId);

        if (type.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getUav().deleteCustomType(type.get().id());

        return Response.ok().build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "uav_monitoring_manage" })
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/monitoring")
    public Response getMonitoringConfiguration(@Context SecurityContext sc,
                                               @PathParam("organization_id") UUID organizationId,
                                               @PathParam("tenant_id") UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        boolean alertOnUnknown = nzyme.getDatabaseCoreRegistry()
                .getValue(UavRegistryKeys.MONITORING_ALERT_ON_UNKNOWN.key(), organizationId, tenantId)
                .map(Boolean::parseBoolean).orElse(false);

        boolean alertOnFriendly = nzyme.getDatabaseCoreRegistry()
                .getValue(UavRegistryKeys.MONITORING_ALERT_ON_FRIENDLY.key(), organizationId, tenantId)
                .map(Boolean::parseBoolean).orElse(false);

        boolean alertOnNeutral = nzyme.getDatabaseCoreRegistry()
                .getValue(UavRegistryKeys.MONITORING_ALERT_ON_NEUTRAL.key(), organizationId, tenantId)
                .map(Boolean::parseBoolean).orElse(false);

        boolean alertOnHostile = nzyme.getDatabaseCoreRegistry()
                .getValue(UavRegistryKeys.MONITORING_ALERT_ON_HOSTILE.key(), organizationId, tenantId)
                .map(Boolean::parseBoolean).orElse(false);

        return Response.ok(UavMonitoringSettingsResponse.create(
                alertOnUnknown,
                alertOnFriendly,
                alertOnNeutral,
                alertOnHostile
        )).build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "uav_monitoring_manage" })
    @Path("/uavs/organization/{organization_id}/tenant/{tenant_id}/monitoring")
    public Response getMonitoringConfiguration(@Context SecurityContext sc,
                                               @PathParam("organization_id") UUID organizationId,
                                               @PathParam("tenant_id") UUID tenantId,
                                               UavMonitoringConfigurationRequest req) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }


        nzyme.getDatabaseCoreRegistry().setValue(
                UavRegistryKeys.MONITORING_ALERT_ON_UNKNOWN.key(),
                String.valueOf(req.alertOnUnknown()),
                organizationId,
                tenantId
        );

        nzyme.getDatabaseCoreRegistry().setValue(
                UavRegistryKeys.MONITORING_ALERT_ON_FRIENDLY.key(),
                String.valueOf(req.alertOnFriendly()),
                organizationId,
                tenantId
        );

        nzyme.getDatabaseCoreRegistry().setValue(
                UavRegistryKeys.MONITORING_ALERT_ON_NEUTRAL.key(),
                String.valueOf(req.alertOnNeutral()),
                organizationId,
                tenantId
        );

        nzyme.getDatabaseCoreRegistry().setValue(
                UavRegistryKeys.MONITORING_ALERT_ON_HOSTILE.key(),
                String.valueOf(req.alertOnHostile()),
                organizationId,
                tenantId
        );

        return Response.ok().build();
    }

    private UavSummaryResponse uavEntryToSummaryResponse(UavEntry uav, Optional<UavTypeMatch> uavType) {
        Double operatorDistanceToUav = null;

        if (uav.latitude() != null && uav.longitude() != null
                && uav.operatorLatitude() != null && uav.operatorLongitude() != null) {
            operatorDistanceToUav = HaversineDistance.haversine(
                    uav.latitude(), uav.longitude(), uav.operatorLatitude(), uav.operatorLongitude()
            );
        }

        ClassificationResponse classification;
        if (uavType.isPresent() && uavType.get().defaultClassification() != null) {
            classification = ClassificationResponse.valueOf(uavType.get().defaultClassification());
        } else {
            // No custom classification. Leave it at the manual classification.
            classification = ClassificationResponse.valueOf(uav.classification());
        }

        return UavSummaryResponse.create(
                uav.lastSeen().isAfter(DateTime.now().minusMinutes(5)),
                uav.identifier(),
                uav.designation(),
                classification,
                UavTypeResponse.fromString(uav.uavType()),
                uavType.map(UavTypeMatch::type).orElse(null),
                uavType.map(UavTypeMatch::model).orElse(null),
                uavType.map(UavTypeMatch::name).orElse(null),
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
