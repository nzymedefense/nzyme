package horse.wtf.nzyme.rest.resources.system;

import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Maps;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.crypto.PGPKeyFingerprint;
import horse.wtf.nzyme.rest.responses.crypto.CryptoMetricsResponse;
import horse.wtf.nzyme.rest.responses.crypto.CryptoResponse;
import horse.wtf.nzyme.rest.responses.crypto.PGPKeyResponse;
import horse.wtf.nzyme.rest.responses.metrics.TimerResponse;
import horse.wtf.nzyme.util.MetricNames;
import horse.wtf.nzyme.util.MetricTools;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/api/system/crypto")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class CryptoResource {

    @Inject
    private NzymeLeader nzyme;

    @GET
    @Path("summary")
    public Response summary() {
        Map<String, PGPKeyResponse> fingerprints = Maps.newHashMap();
        for (PGPKeyFingerprint fp : nzyme.getCrypto().getPGPKeysByNode()) {
            fingerprints.put(fp.node(), PGPKeyResponse.create(fp.node(), fp.fingerprint(), fp.createdAt()));
        }

        CryptoMetricsResponse metrics = CryptoMetricsResponse.create(
                TimerResponse.fromTimer(MetricTools.getTimer(nzyme.getMetrics(), MetricNames.PGP_ENCRYPTION_TIMING)),
                TimerResponse.fromTimer(MetricTools.getTimer(nzyme.getMetrics(), MetricNames.PGP_DECRYPTION_TIMING))
        );

        return Response.ok(CryptoResponse.create(metrics, fingerprints)).build();
    }

}
