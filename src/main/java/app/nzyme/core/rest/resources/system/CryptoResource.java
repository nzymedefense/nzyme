package app.nzyme.core.rest.resources.system;

import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Maps;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.PGPKeyFingerprint;
import app.nzyme.core.rest.responses.crypto.CryptoMetricsResponse;
import app.nzyme.core.rest.responses.crypto.CryptoResponse;
import app.nzyme.core.rest.responses.crypto.PGPKeyResponse;
import app.nzyme.core.rest.responses.metrics.TimerResponse;
import app.nzyme.core.util.MetricNames;
import app.nzyme.core.util.MetricTools;

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
    private NzymeNode nzyme;

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
