package horse.wtf.nzyme.rest.resources.taps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.configuration.db.BaseConfigurationService;
import app.nzyme.plugin.rest.security.RESTSecured;
import horse.wtf.nzyme.rest.responses.taps.metrics.TapMetricsGaugeHistogramResponse;
import horse.wtf.nzyme.rest.responses.taps.metrics.TapMetricsGaugeHistogramValueResponse;
import horse.wtf.nzyme.taps.metrics.BucketSize;
import horse.wtf.nzyme.taps.metrics.TapMetrics;
import horse.wtf.nzyme.taps.metrics.TapMetricsGauge;
import horse.wtf.nzyme.rest.responses.taps.*;
import horse.wtf.nzyme.rest.responses.taps.metrics.TapMetricsGaugeResponse;
import horse.wtf.nzyme.rest.responses.taps.metrics.TapMetricsResponse;
import horse.wtf.nzyme.taps.Bus;
import horse.wtf.nzyme.taps.Capture;
import horse.wtf.nzyme.taps.Channel;
import horse.wtf.nzyme.taps.Tap;
import horse.wtf.nzyme.taps.metrics.TapMetricsGaugeAggregation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/api/taps")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class TapsResource {

    private static final Logger LOG = LogManager.getLogger(TapsResource.class);

    @Inject
    private NzymeLeader nzyme;

    @GET
    public Response findAll() {
        List<TapDetailsResponse> tapsResponse = Lists.newArrayList();
        Optional<List<Tap>> taps = nzyme.getTapManager().findAllTaps();
        if (taps.isPresent()) {
            for (Tap tap : taps.get()) {
                tapsResponse.add(buildTapResponse(tap));
            }
        }

        return Response.ok(TapListResponse.create(tapsResponse.size(), tapsResponse)).build();
    }

    @GET
    @Path("/show/{name}")
    public Response findTap(@PathParam("name") String name) {
        Optional<Tap> tap = nzyme.getTapManager().findTap(name);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(buildTapResponse(tap.get())).build();
        }
    }

    @GET
    @Path("/show/{name}/metrics")
    public Response tapMetrics(@PathParam("name") String name) {
        Optional<Tap> tap = nzyme.getTapManager().findTap(name);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        TapMetrics metrics = nzyme.getTapManager().findMetricsOfTap(tap.get().name());
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
    @Path("/show/{tapName}/metrics/gauges/{metricName}/histogram")
    public Response tapMetricsGauge(@PathParam("tapName") String name, @PathParam("metricName") String metricName) {
        Optional<Tap> tap = nzyme.getTapManager().findTap(name);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Map<DateTime, TapMetricsGaugeAggregation>> histo = nzyme.getTapManager().findMetricsHistogram(
                tap.get().name(), metricName, 24, BucketSize.MINUTE
        );

        if (histo.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Map<DateTime, TapMetricsGaugeHistogramValueResponse> result = Maps.newHashMap();
        for (TapMetricsGaugeAggregation value : histo.get().values()) {
            result.put(value.bucket(), TapMetricsGaugeHistogramValueResponse.create(
                    value.bucket(), value.average(), value.maximum(), value.minimum()
            ));
        }

        return Response.ok(TapMetricsGaugeHistogramResponse.create(result)).build();
    }

    @GET
    @Path("/secret")
    public Response getTapSecret() {
       return Response.ok(TapSecretResponse.create(
               nzyme.getConfigurationService().getConfiguration().tapSecret()
       )).build();
    }

    @POST
    @Path("/secret/cycle")
    public Response cycleTapSecret() {
        BaseConfigurationService c = nzyme.getConfigurationService();
        String newSecret = c.generateTapSecret();
        c.setTapSecret(newSecret);

        LOG.info("Cycled tap secret via REST request.");

        return Response.ok(TapSecretResponse.create(newSecret)).build();
    }

    private TapDetailsResponse buildTapResponse(Tap tap) {
        List<BusDetailsResponse> busesResponse = Lists.newArrayList();

        Optional<List<Bus>> buses = nzyme.getTapManager().findBusesOfTap(tap.name());
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
        Optional<List<Capture>> captures = nzyme.getTapManager().findCapturesOfTap(tap.name());
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
                tap.name(),
                tap.localTime(),
                TotalWithAverageResponse.create(tap.processedBytes().total(), tap.processedBytes().average()),
                tap.memoryTotal(),
                tap.memoryFree(),
                tap.memoryUsed(),
                tap.cpuLoad(),
                tap.updatedAt().isAfter(DateTime.now().minusMinutes(2)),
                tap.createdAt(),
                tap.updatedAt(),
                "",
                busesResponse,
                capturesResponse
        );
    }

}
