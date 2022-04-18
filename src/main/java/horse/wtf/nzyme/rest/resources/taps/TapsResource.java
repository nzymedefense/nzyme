package horse.wtf.nzyme.rest.resources.taps;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.configuration.db.BaseConfigurationService;
import horse.wtf.nzyme.rest.authentication.RESTSecured;
import horse.wtf.nzyme.rest.responses.taps.TapSecretResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/taps")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class TapsResource {

    private static final Logger LOG = LogManager.getLogger(TapsResource.class);

    @Inject
    private NzymeLeader nzyme;

    @POST
    public Response findAll() {
        return Response.ok().build();
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
