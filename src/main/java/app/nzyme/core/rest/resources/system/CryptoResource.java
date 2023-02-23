package app.nzyme.core.rest.resources.system;

import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.crypto.PGPKeyFingerprint;
import app.nzyme.core.crypto.tls.TLSKeyAndCertificate;
import app.nzyme.core.crypto.tls.TLSUtils;
import app.nzyme.core.distributed.MetricExternalName;
import app.nzyme.core.distributed.Node;
import app.nzyme.core.distributed.database.metrics.TimerSnapshot;
import app.nzyme.core.rest.responses.crypto.*;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.responses.metrics.TimerResponse;
import com.google.common.collect.Sets;
import com.google.common.math.Stats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Path("/api/system/crypto")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class CryptoResource {

    private static final Logger LOG = LogManager.getLogger(CryptoResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("summary")
    public Response summary() {
        Map<String, PGPKeyResponse> fingerprints = Maps.newHashMap();
        for (PGPKeyFingerprint fp : nzyme.getCrypto().getPGPKeysByNode()) {
            fingerprints.put(fp.nodeName(), PGPKeyResponse.create(fp.nodeName(), fp.fingerprint(), fp.createdAt()));
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

        Map<String, TLSCertificateResponse> tlsCertificates = Maps.newTreeMap();
        for (TLSKeyAndCertificate cert : nzyme.getCrypto().getTLSCertificateByNode()) {
            Optional<Node> node = nzyme.getNodeManager().getNode(cert.nodeId());
            if (node.isPresent() && node.get().lastSeen().isAfter(DateTime.now().minusMinutes(2))) {
                String nodeName = nzyme.getNodeManager().findNameOfNode(cert.nodeId());
                tlsCertificates.put(
                        nodeName,
                        TLSCertificateResponse.create(
                                cert.nodeId().toString(),
                                nodeName,
                                cert.signature(),
                                cert.expiresAt()
                        )
                );
            }
        }

        return Response.ok(CryptoResponse.create(
                metrics,
                fingerprints,
                tlsCertificates,
                nzyme.getCrypto().allPGPKeysEqualAcrossCluster()
        )).build();
    }

    @GET
    @Path("/tls/node/{node_id}")
    public Response tlsCertificate(@PathParam("node_id") UUID nodeId) {
        Optional<Node> node = nzyme.getNodeManager().getNode(nodeId);
        if (node.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<TLSKeyAndCertificate> tls = nzyme.getCrypto().getTLSCertificateOfNode(nodeId);

        if (tls.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        TLSKeyAndCertificate cert = tls.get();

        return Response.ok(TLSCertificateResponse.create(
                cert.nodeId().toString(),
                node.get().name(),
                cert.signature(),
                cert.expiresAt()
        )).build();
    }

    @PUT
    @Path("/tls/node/{node_id}/regenerate")
    public Response regenerateTLSCertificate(@PathParam("node_id") UUID nodeId) {
        if (nzyme.getNodeManager().getNode(nodeId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Crypto crypto = nzyme.getCrypto();
        try {
            crypto.updateTLSCertificateOfNode(
                    nodeId,
                    crypto.generateTLSCertificate(Crypto.DEFAULT_TLS_SUBJECT_DN, 12)
            );
            nzyme.reloadHttpServer(5, TimeUnit.SECONDS); // Graceful, async shutdown to let this call finish. TODO perform on correct node
        } catch (Crypto.CryptoOperationException e) {
            LOG.error("Could not generate TLS certificate.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/tls/node/{node_id}")
    public Response uploadTLSCertificate(@PathParam("node_id") UUID nodeId,
                                         @FormDataParam("certificate") InputStream certificate,
                                         @FormDataParam("private_key") InputStream privateKey) {
        if (nzyme.getNodeManager().getNode(nodeId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String certificateInput, keyInput;
        try {
            certificateInput = new String(certificate.readAllBytes());
            keyInput = new String(privateKey.readAllBytes());
        } catch (Exception e) {
            LOG.error("Could not read provided TLS certificate form data.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        List<X509Certificate> certificates;
        PrivateKey key;
        try {
            certificates = TLSUtils.readCertificateChainFromPEM(certificateInput);
            key = TLSUtils.readKeyFromPEM(keyInput);
        }catch(Exception e) {
            LOG.error("Could not build key/certificate from provided data.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        // We have a valid certificate and key from here on. Serialize to Base64 and store.
        X509Certificate firstCert = certificates.get(0);
        String fingerprint;

        try {
            fingerprint = TLSUtils.calculateTLSCertificateFingerprint(firstCert);
        } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
            LOG.error("Could not build certificate fingerprint.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        TLSKeyAndCertificate tls = TLSKeyAndCertificate.create(
                nodeId,
                certificates,
                key,
                fingerprint,
                new DateTime(firstCert.getNotBefore()),
                new DateTime(firstCert.getNotAfter())
        );

        nzyme.getCrypto().updateTLSCertificateOfNode(nodeId, tls);
        nzyme.reloadHttpServer(5, TimeUnit.SECONDS); // Graceful, async shutdown to let this call finish. TODO perform on correct node

        return Response.ok(Response.Status.CREATED).build();
    }

}
