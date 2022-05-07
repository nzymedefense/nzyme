package horse.wtf.nzyme.rest.resources.taps;

import com.google.common.collect.Lists;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.configuration.db.BaseConfigurationService;
import horse.wtf.nzyme.rest.authentication.RESTSecured;
import horse.wtf.nzyme.rest.responses.taps.*;
import horse.wtf.nzyme.taps.Bus;
import horse.wtf.nzyme.taps.Channel;
import horse.wtf.nzyme.taps.Tap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
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
                busesResponse
        );
    }

}
