package app.nzyme.core.rest.resources.system;

import app.nzyme.core.distributed.MetricExternalName;
import app.nzyme.core.distributed.database.metrics.TimerSnapshot;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.PGPKeyFingerprint;
import app.nzyme.core.rest.responses.crypto.CryptoMetricsResponse;
import app.nzyme.core.rest.responses.crypto.CryptoResponse;
import app.nzyme.core.rest.responses.crypto.PGPKeyResponse;
import app.nzyme.core.rest.responses.metrics.TimerResponse;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        Map<UUID, TimerSnapshot> encryption = nzyme.getClusterManager().findMetricTimer(
                MetricExternalName.PGP_ENCRYPTION_TIMER.database_label
        );

        Map<UUID, TimerSnapshot> decryption = nzyme.getClusterManager().findMetricTimer(
                MetricExternalName.PGP_DECRYPTION_TIMER.database_label
        );

        Map<String, CryptoMetricsResponse> metrics = Maps.newTreeMap();

        List<UUID> nodeIds = Lists.newArrayList();
        nodeIds.addAll(encryption.keySet());
        nodeIds.addAll(decryption.keySet());

        for (UUID nodeId : nodeIds) {
            TimerSnapshot nodeEncryption = encryption.get(nodeId);
            TimerSnapshot nodeDecryption = decryption.get(nodeId);
            if (nodeEncryption != null) {
                metrics.put(nodeId.toString(), CryptoMetricsResponse.create(
                        TimerResponse.create(
                                nodeEncryption.mean(),
                                nodeEncryption.max(),
                                nodeEncryption.min(),
                                nodeEncryption.stddev(),
                                nodeEncryption.p99(),
                                nodeEncryption.counter()
                        ),
                        TimerResponse.create(
                                nodeDecryption.mean(),
                                nodeDecryption.max(),
                                nodeDecryption.min(),
                                nodeDecryption.stddev(),
                                nodeDecryption.p99(),
                                nodeDecryption.counter()
                        )
                ));
            }
        }

        // TODO: extend with quick aggregation over all nodes in Metrics.

        return Response.ok(CryptoResponse.create(metrics, fingerprints)).build();
    }

}
