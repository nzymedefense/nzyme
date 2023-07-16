package app.nzyme.core.rest.resources.taps;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.plugin.rest.security.PermissionLevel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.plugin.rest.security.RESTSecured;
import app.nzyme.core.rest.responses.taps.metrics.TapMetricsGaugeHistogramResponse;
import app.nzyme.core.rest.responses.taps.metrics.TapMetricsGaugeHistogramValueResponse;
import app.nzyme.core.taps.metrics.BucketSize;
import app.nzyme.core.taps.metrics.TapMetrics;
import app.nzyme.core.taps.metrics.TapMetricsGauge;
import app.nzyme.core.rest.responses.taps.*;
import app.nzyme.core.rest.responses.taps.metrics.TapMetricsGaugeResponse;
import app.nzyme.core.rest.responses.taps.metrics.TapMetricsResponse;
import app.nzyme.core.taps.Bus;
import app.nzyme.core.taps.Capture;
import app.nzyme.core.taps.Channel;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.taps.metrics.TapMetricsGaugeAggregation;
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
            tapsResponse.add(TapHighLevelInformationDetailsResponse.create(tap.uuid(), tap.name()));
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

        TapMetrics metrics = nzyme.getTapManager().findMetricsOfTap(uuid);
        List<TapMetricsGauge> gauges = metrics.gauges();

        Map<String, TapMetricsGaugeResponse> parsedGauges = Maps.newHashMap();
        for (TapMetricsGauge gauge : gauges) {
            parsedGauges.put(
                    gauge.metricName(),
                    TapMetricsGaugeResponse.create(
                            gauge.metricName(),
                            gauge.metricValue(),
                            gauge.createdAt()
                    )
            );
        }

        return Response.ok(
                TapMetricsResponse.create(
                        parsedGauges
                )
        ).build();
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

        Optional<Map<DateTime, TapMetricsGaugeAggregation>> histo = nzyme.getTapManager().findMetricsHistogram(
                uuid, metricName, 24, BucketSize.MINUTE
        );

        if (histo.isEmpty()) {
            return Response.ok(Maps.newHashMap()).build();
        }

        Map<DateTime, TapMetricsGaugeHistogramValueResponse> result = Maps.newTreeMap();
        for (TapMetricsGaugeAggregation value : histo.get().values()) {
            result.put(value.bucket(), TapMetricsGaugeHistogramValueResponse.create(
                    value.bucket(), value.average(), value.maximum(), value.minimum()
            ));
        }

        return Response.ok(TapMetricsGaugeHistogramResponse.create(result)).build();
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
        Optional<List<Capture>> captures = nzyme.getTapManager().findCapturesOfTap(tap.uuid());
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
                                capture.updatedAt(),
                                capture.createdAt()
                        )
                );
            }
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
                tap.lastReport() == null ? false : tap.lastReport().isAfter(DateTime.now().minusMinutes(2)),
                tap.clockDriftMs(),
                tap.createdAt(),
                tap.updatedAt(),
                tap.lastReport(),
                tap.description(),
                busesResponse,
                capturesResponse
        );
    }

}
