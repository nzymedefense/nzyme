package horse.wtf.nzyme.rest.resources.taps;

import com.google.common.collect.Lists;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.configuration.db.BaseConfigurationService;
import horse.wtf.nzyme.rest.authentication.RESTSecured;
import horse.wtf.nzyme.rest.responses.taps.TapDetailsResponse;
import horse.wtf.nzyme.rest.responses.taps.TapListResponse;
import horse.wtf.nzyme.rest.responses.taps.TapSecretResponse;
import horse.wtf.nzyme.taps.Tap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/taps")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class TapsResource {

    private static final Logger LOG = LogManager.getLogger(TapsResource.class);

    @Inject
    private NzymeLeader nzyme;

    @GET
    public Response findAll() {
        List<TapDetailsResponse> taps = Lists.newArrayList();
        for (Tap tap : nzyme.getTapManager().findAllTaps()) {
            taps.add(TapDetailsResponse.create(
                    tap.name(),
                    tap.localTime(),
                    tap.processedBytesTotal(),
                    tap.processedBytes10(),
                    tap.memoryTotal(),
                    tap.memoryFree(),
                    tap.memoryUsed(),
                    tap.cpuLoad(),
                    tap.createdAt(),
                    tap.updatedAt(),
                    ""
            ));
        }

        return Response.ok(TapListResponse.create(taps.size(), taps)).build();
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

}
