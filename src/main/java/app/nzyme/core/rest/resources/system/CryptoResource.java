package app.nzyme.core.rest.resources.system;

import app.nzyme.core.distributed.MetricExternalName;
import app.nzyme.core.distributed.database.metrics.TimerSnapshot;
import app.nzyme.core.rest.responses.crypto.CryptoNodeMetricsResponse;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.PGPKeyFingerprint;
import app.nzyme.core.rest.responses.crypto.CryptoMetricsResponse;
import app.nzyme.core.rest.responses.crypto.CryptoResponse;
import app.nzyme.core.rest.responses.crypto.PGPKeyResponse;
import app.nzyme.core.rest.responses.metrics.TimerResponse;
import com.google.common.collect.Sets;
import com.google.common.math.Stats;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        List<String> uniqueFingerprints = Lists.newArrayList();
        for (PGPKeyFingerprint fp : nzyme.getCrypto().getPGPKeysByNode()) {
            fingerprints.put(fp.node(), PGPKeyResponse.create(fp.node(), fp.fingerprint(), fp.createdAt()));
            uniqueFingerprints.add(fp.fingerprint());
        }

        Map<UUID, TimerSnapshot> encryption = nzyme.getClusterManager().findMetricTimer(
                MetricExternalName.PGP_ENCRYPTION_TIMER.database_label
        );

        Map<UUID, TimerSnapshot> decryption = nzyme.getClusterManager().findMetricTimer(
                MetricExternalName.PGP_DECRYPTION_TIMER.database_label
        );

        Map<String, CryptoMetricsResponse> nodeMetrics = Maps.newTreeMap();

        List<UUID> nodeIds = Lists.newArrayList();
        nodeIds.addAll(encryption.keySet());
        nodeIds.addAll(decryption.keySet());

        Set<Long> encryptionMeans = Sets.newHashSet();
        Set<Long> encryptionMaxs = Sets.newHashSet();
        Set<Long> encryptionMins = Sets.newHashSet();
        Set<Long> encryptionStddevs = Sets.newHashSet();
        Set<Long> encryptionP99s = Sets.newHashSet();
        Set<Long> encryptionCounters = Sets.newHashSet();
        Set<Long> decryptionMeans = Sets.newHashSet();
        Set<Long> decryptionMaxs = Sets.newHashSet();
        Set<Long> decryptionMins = Sets.newHashSet();
        Set<Long> decryptionStddevs = Sets.newHashSet();
        Set<Long> decryptionP99s = Sets.newHashSet();
        Set<Long> decryptionCounters = Sets.newHashSet();

        for (UUID nodeId : nodeIds) {
            TimerSnapshot nodeEncryption = encryption.get(nodeId);
            TimerSnapshot nodeDecryption = decryption.get(nodeId);
            if (nodeEncryption != null) {
                if (nodeEncryption.mean() > 0) encryptionMeans.add(nodeEncryption.mean());
                if (nodeEncryption.max() > 0) encryptionMaxs.add(nodeEncryption.max());
                if (nodeEncryption.min() > 0) encryptionMins.add(nodeEncryption.min());
                if (nodeEncryption.stddev() > 0) encryptionStddevs.add(nodeEncryption.stddev());
                if (nodeEncryption.p99() > 0) encryptionP99s.add(nodeEncryption.p99());
                if (nodeEncryption.counter() > 0) encryptionCounters.add(nodeEncryption.counter());

                if (nodeDecryption.mean() > 0) decryptionMeans.add(nodeDecryption.mean());
                if (nodeDecryption.max() > 0) decryptionMaxs.add(nodeDecryption.max());
                if (nodeDecryption.min() > 0) decryptionMins.add(nodeDecryption.min());
                if (nodeDecryption.stddev() > 0) decryptionStddevs.add(nodeDecryption.stddev());
                if (nodeDecryption.p99() > 0) decryptionP99s.add(nodeDecryption.p99());
                if (nodeDecryption.counter() > 0) decryptionCounters.add(nodeDecryption.counter());

                nodeMetrics.put(nzyme.getNodeManager().findNameOfNode(nodeId), CryptoMetricsResponse.create(
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

        CryptoMetricsResponse clusterMetrics = CryptoMetricsResponse.create(
                TimerResponse.create(
                        encryptionMeans.isEmpty() ? 0 : Stats.meanOf(encryptionMeans),
                        encryptionMaxs.isEmpty() ? 0 : Stats.of(encryptionMaxs).max(),
                        encryptionMins.isEmpty() ? 0 :  Stats.of(encryptionMins).min(),
                        encryptionStddevs.isEmpty() ? 0 : Stats.meanOf(encryptionStddevs),
                        encryptionP99s.isEmpty() ? 0 : Stats.of(encryptionP99s).max(),
                        encryptionCounters.isEmpty() ? 0 : ((Double) Stats.of(encryptionCounters).sum()).longValue()
                ),
                TimerResponse.create(
                        decryptionMeans.isEmpty() ? 0 : Stats.meanOf(decryptionMeans),
                        decryptionMaxs.isEmpty() ? 0 : Stats.of(decryptionMaxs).max(),
                        decryptionMins.isEmpty() ? 0 : Stats.of(decryptionMins).min(),
                        decryptionStddevs.isEmpty() ? 0 : Stats.meanOf(decryptionStddevs),
                        decryptionP99s.isEmpty() ? 0 : Stats.of(decryptionP99s).max(),
                        decryptionCounters.isEmpty() ? 0 : ((Double) Stats.of(decryptionCounters).sum()).longValue()
                )
        );

        CryptoNodeMetricsResponse metrics = CryptoNodeMetricsResponse.create(nodeMetrics, clusterMetrics);


        return Response.ok(
                CryptoResponse.create(metrics, fingerprints, uniqueFingerprints.size() == 1)
        ).build();
    }

}
