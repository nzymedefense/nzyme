package app.nzyme.core.rest.resources.timelines;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.floorplans.db.TenantLocationEntry;
import app.nzyme.core.floorplans.db.TenantLocationFloorEntry;
import app.nzyme.core.rest.RestTools;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.responses.taps.TapHighLevelInformationDetailsResponse;
import app.nzyme.core.rest.responses.timelines.TimelineActivityHistogramBucketResponse;
import app.nzyme.core.rest.responses.timelines.TimelineActivityHistogramResponse;
import app.nzyme.core.rest.responses.timelines.TimelineEventDetailsResponse;
import app.nzyme.core.rest.responses.timelines.TimelineResponse;
import app.nzyme.core.shared.db.TapBasedSignalStrengthResult;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.timelines.TimelineAddressType;
import app.nzyme.core.timelines.Timelines;
import app.nzyme.core.timelines.TimelinesRegistryKeys;
import app.nzyme.core.timelines.db.TimelineActivityHistogram;
import app.nzyme.core.timelines.db.TimelineActivityHistogramBucket;
import app.nzyme.core.timelines.db.TimelineEventEntry;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/timelines")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class TimelinesResource extends UserAuthenticatedResource {

    @Inject
    private NzymeNode nzyme;

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JodaModule())
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private final static int GAP_THRESHOLD = 10;

    @GET
    @Path("/show/type/{addressType}/address/{address}")
    public Response findOne(@Context SecurityContext sc,
                            @PathParam("addressType") TimelineAddressType addressType,
                            @PathParam("address") String address,
                            @QueryParam("organization_id") @NotNull UUID organizationId,
                            @QueryParam("tenant_id") @NotNull UUID tenantId,
                            @QueryParam("time_range") String timeRangeParameter,
                            @QueryParam("excluded_event_types") String excludedEventTypesP,
                            @QueryParam("time_zone") String timeZone,
                            @QueryParam("limit") int limit,
                            @QueryParam("offset") int offset) {
        TimeRange timeRange = parseTimeRangeQueryParameter(timeRangeParameter);

        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        List<String> excludedEventTypes = this.objectMapper.readValue(excludedEventTypesP, new TypeReference<>() {});

        Timelines timelines = new Timelines(nzyme);
        long total = timelines.countAllEventsOfAddress(
                organizationId, tenantId, addressType, address, timeRange, excludedEventTypes, GAP_THRESHOLD
        );

        List<TimelineEventDetailsResponse> events = Lists.newArrayList();
        for (TimelineEventEntry e : timelines.findAllEventsOfAddress(
                organizationId, tenantId, addressType, address, timeRange, excludedEventTypes, GAP_THRESHOLD, limit, offset)) {

            events.add(TimelineEventDetailsResponse.create(
                    e.uuid(),
                    e.address(),
                    e.addressType(),
                    e.eventType(),
                    objectMapper.readValue(e.eventDetails(), new TypeReference<>(){}),
                    e.timestamp()
            ));
        }

        List<UUID> tenantTapIds = nzyme.getTapManager()
                .findAllTapsOfTenant(organizationId, tenantId)
                .stream().map(Tap::uuid).toList();

        List<String> ssids = nzyme.getDot11().findSSIDsAdvertisedByBSSID(address, tenantTapIds, timeRange);
        List<String> fingerprints = nzyme.getDot11().findFingerprintsOfBSSID(address, timeRange, tenantTapIds);
        List<TapHighLevelInformationDetailsResponse> recordingTaps = Lists.newArrayList();
        for (TapBasedSignalStrengthResult recordingTap : nzyme.getDot11()
                .findBSSIDSignalStrengthPerTap(address, timeRange, tenantTapIds)) {
            Optional<Tap> tap = nzyme.getTapManager().findTap(recordingTap.tapUuid());

            if (tap.isEmpty()) {
                continue;
            }

            String locationName;
            String floorName;
            if (tap.get().locationId() != null) {
                locationName = nzyme.getAuthenticationService()
                        .findTenantLocation(tap.get().locationId(), organizationId, tenantId)
                        .map(TenantLocationEntry::name)
                        .orElse(null);

                if (locationName != null && tap.get().floorId() != null) {
                    floorName = nzyme.getAuthenticationService()
                            .findFloorOfTenantLocation(tap.get().locationId(), tap.get().floorId())
                            .map(Tools::buildFloorName)
                            .orElse(null);
                } else {
                    floorName = null;
                }
            } else {
                locationName = null;
                floorName = null;
            }

            recordingTaps.add(TapHighLevelInformationDetailsResponse.create(
                    tap.get().uuid(),
                    tap.get().name(),
                    locationName,
                    floorName,
                    Tools.isTapActive(tap.get().lastReport())
            ));
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        int retentionTimeDays = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                .getValue(TimelinesRegistryKeys.DOT11_EVENTS_RETENTION_TIME_DAYS.key(), organizationId, tenantId)
                .orElse(TimelinesRegistryKeys.DOT11_EVENTS_RETENTION_TIME_DAYS.defaultValue().get()));

        String tz;
        try {
            tz = ZoneId.of(timeZone).getId();
        } catch (Exception e) {
            tz = "UTC";
        }

        TimelineActivityHistogram ah = timelines.getEventTypeActivityHistogram(
                organizationId, tenantId, addressType, address, timeRange, excludedEventTypes, GAP_THRESHOLD, tz
        );

        List<TimelineActivityHistogramBucketResponse> buckets = Lists.newArrayList();
        for (TimelineActivityHistogramBucket bucket : ah.buckets()) {
            buckets.add(TimelineActivityHistogramBucketResponse.create(
                    bucket.bucket(), bucket.total(), bucket.countsByEventType())
            );
        }

        TimelineActivityHistogramResponse activityHistogramResponse = TimelineActivityHistogramResponse.create(
                ah.from(), ah.to(), ah.bucketType().toString(), ah.totalsByEventType(), buckets
        );

        return Response.ok(TimelineResponse.create(
                retentionTimeDays,
                total,
                events,
                ssids,
                fingerprints,
                recordingTaps,
                activityHistogramResponse
        )).build();
    }

}
