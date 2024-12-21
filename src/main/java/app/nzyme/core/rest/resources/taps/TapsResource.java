package app.nzyme.core.rest.resources.taps;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.taps.metrics.*;
import app.nzyme.core.taps.db.metrics.*;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.plugin.rest.security.RESTSecured;
import app.nzyme.core.rest.responses.taps.*;
import app.nzyme.core.taps.Bus;
import app.nzyme.core.taps.Capture;
import app.nzyme.core.taps.Channel;
import app.nzyme.core.taps.Tap;
import org.joda.time.DateTime;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Path("/api/taps")
@Produces(MediaType.APPLICATION_JSON)
public class TapsResource extends UserAuthenticatedResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(PermissionLevel.ANY)
    @Path("/highlevel")
    public Response findAllWithHighLevelInformation(@Context SecurityContext sc) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        // Get all UUIDs of taps the user can access.
        List<UUID> uuids = nzyme.getTapManager().allTapUUIDsAccessibleByUser(authenticatedUser);

        List<TapHighLevelInformationDetailsResponse> tapsResponse = Lists.newArrayList();
        for (Tap tap : nzyme.getTapManager().findAllTapsByUUIDs(uuids)) {
            tapsResponse.add(TapHighLevelInformationDetailsResponse.create(
                    tap.uuid(), tap.name(), Tools.isTapActive(tap.lastReport())
            ));
        }

        return Response.ok(TapHighLevelInformationListResponse.create(tapsResponse)).build();
    }

    @GET
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    public Response findAll(@Context SecurityContext sc) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        // Get all UUIDs of taps the user can access.
        List<UUID> uuids = nzyme.getTapManager().allTapUUIDsAccessibleByUser(authenticatedUser);

        List<TapDetailsResponse> tapsResponse = Lists.newArrayList();
        for (Tap tap : nzyme.getTapManager().findAllTapsByUUIDs(uuids)) {
            tapsResponse.add(buildTapResponse(tap));
        }

        return Response.ok(TapListResponse.create(tapsResponse.size(), tapsResponse)).build();
    }

    @GET
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{uuid}")
    public Response findTap(@Context SecurityContext sc, @PathParam("uuid") UUID uuid) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!nzyme.getTapManager().allTapUUIDsAccessibleByUser(authenticatedUser).contains(uuid)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<Tap> tap = nzyme.getTapManager().findTap(uuid);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(buildTapResponse(tap.get())).build();
        }
    }

    @GET
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{uuid}/metrics")
    public Response tapMetrics(@Context SecurityContext sc, @PathParam("uuid") UUID uuid) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!nzyme.getTapManager().allTapUUIDsAccessibleByUser(authenticatedUser).contains(uuid)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<Tap> tap = nzyme.getTapManager().findTap(uuid);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Map<String, TapMetricsGaugeResponse> parsedGauges = Maps.newHashMap();
        for (TapMetricsGauge gauge : nzyme.getTapManager().findGaugesOfTap(uuid)) {
            if (gauge.metricName().startsWith("captures") || gauge.metricName().startsWith("channels")) {
                continue;
            }

            parsedGauges.put(
                    gauge.metricName(),
                    TapMetricsGaugeResponse.create(
                            gauge.metricName(),
                            gauge.metricValue(),
                            gauge.createdAt()
                    )
            );
        }

        Map<String, TapMetricsTimerResponse> parsedTimers = Maps.newHashMap();
        for (TapMetricsTimer timer : nzyme.getTapManager().findTimersOfTap(uuid)) {
            parsedTimers.put(
                    timer.metricName(),
                    TapMetricsTimerResponse.create(
                            timer.metricName(),
                            timer.mean(),
                            timer.p99(),
                            timer.createdAt()
                    )
            );
        }

        return Response.ok(TapMetricsResponse.create(parsedGauges, parsedTimers)).build();
    }

    @GET
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{uuid}/metrics/gauges/{metricName}/histogram")
    public Response tapMetricsGauge(@Context SecurityContext sc,
                                    @PathParam("uuid") UUID uuid,
                                    @PathParam("metricName") String metricName) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!nzyme.getTapManager().allTapUUIDsAccessibleByUser(authenticatedUser).contains(uuid)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<Tap> tap = nzyme.getTapManager().findTap(uuid);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Map<DateTime, TapMetricsAggregation>> histo = nzyme.getTapManager().findMetricsGaugeHistogram(
                uuid, metricName, 24, BucketSize.MINUTE
        );

        if (histo.isEmpty()) {
            return Response.ok(Maps.newHashMap()).build();
        }

        Map<DateTime, TapMetricsHistogramValueResponse> result = Maps.newTreeMap();
        for (TapMetricsAggregation value : histo.get().values()) {
            result.put(value.bucket(), TapMetricsHistogramValueResponse.create(
                    value.bucket(), value.average(), value.maximum(), value.minimum()
            ));
        }

        return Response.ok(TapMetricsHistogramResponse.create(result)).build();
    }

    @GET
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/show/{uuid}/metrics/timers/{metricName}/histogram")
    public Response tapMetricsTimer(@Context SecurityContext sc,
                                    @PathParam("uuid") UUID uuid,
                                    @PathParam("metricName") String metricName) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!nzyme.getTapManager().allTapUUIDsAccessibleByUser(authenticatedUser).contains(uuid)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<Tap> tap = nzyme.getTapManager().findTap(uuid);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Map<DateTime, TapMetricsAggregation>> histo = nzyme.getTapManager().findMetricsTimerHistogram(
                uuid, metricName, 24, BucketSize.MINUTE
        );

        if (histo.isEmpty()) {
            return Response.ok(Maps.newHashMap()).build();
        }

        Map<DateTime, TapMetricsHistogramValueResponse> result = Maps.newTreeMap();
        for (TapMetricsAggregation value : histo.get().values()) {
            result.put(value.bucket(), TapMetricsHistogramValueResponse.create(
                    value.bucket(), value.average(), value.maximum(), value.minimum()
            ));
        }

        return Response.ok(TapMetricsHistogramResponse.create(result)).build();
    }

    private TapDetailsResponse buildTapResponse(Tap tap) {
        List<BusDetailsResponse> busesResponse = Lists.newArrayList();

        Optional<List<Bus>> buses = nzyme.getTapManager().findBusesOfTap(tap.uuid());
        if (buses.isPresent()) {
            for (Bus bus : buses.get()) {
                List<ChannelDetailsResponse> channelsResponse = Lists.newArrayList();

                Optional<List<Channel>> channels = nzyme.getTapManager().findChannelsOfBus(bus.id());
                if (channels.isPresent()) {
                    for (Channel channel : channels.get()) {
                        channelsResponse.add(ChannelDetailsResponse.create(
                                channel.name(),
                                channel.capacity(),
                                channel.watermark(),
                                TotalWithAverageResponse.create(
                                        channel.errors().total(),
                                        channel.errors().average()
                                ),
                                TotalWithAverageResponse.create(
                                        channel.throughputBytes().total(),
                                        channel.throughputBytes().average()
                                ),
                                TotalWithAverageResponse.create(
                                        channel.throughputMessages().total(),
                                        channel.throughputMessages().average()
                                )
                        ));
                    }
                }

                busesResponse.add(BusDetailsResponse.create(
                        bus.id(),
                        bus.name(),
                        channelsResponse
                ));
            }
        }

        List<CaptureDetailsResponse> capturesResponse = Lists.newArrayList();
        Optional<List<Capture>> captures = nzyme.getTapManager().findActiveCapturesOfTap(tap.uuid());
        if (captures.isPresent()) {
            for (Capture capture : captures.get()) {
                capturesResponse.add(
                        CaptureDetailsResponse.create(
                                capture.interfaceName(),
                                capture.captureType(),
                                capture.isRunning(),
                                capture.received(),
                                capture.droppedBuffer(),
                                capture.droppedInterface(),
                                capture.cycleTime(),
                                capture.updatedAt(),
                                capture.createdAt()
                        )
                );
            }
        }

        List<TapFrequencyAndChannelWidthsResponse> dot11Frequencies = Lists.newArrayList();
        for (Dot11FrequencyAndChannelWidthEntry fcw : nzyme.getTapManager().findDot11FrequenciesOfTap(tap.uuid())) {
            dot11Frequencies.add(TapFrequencyAndChannelWidthsResponse.create(fcw.frequency(), fcw.channelWidths()));
        }

        return TapDetailsResponse.create(
                tap.uuid(),
                tap.name(),
                tap.version(),
                tap.clock(),
                TotalWithAverageResponse.create(tap.processedBytes().total(), tap.processedBytes().average()),
                tap.memoryTotal(),
                tap.memoryFree(),
                tap.memoryUsed(),
                tap.cpuLoad(),
                Tools.isTapActive(tap.lastReport()),
                tap.clockDriftMs(),
                tap.createdAt(),
                tap.updatedAt(),
                tap.lastReport(),
                tap.description(),
                busesResponse,
                capturesResponse,
                tap.remoteAddress(),
                dot11Frequencies,
                tap.organizationId(),
                tap.tenantId()
        );
    }

}
