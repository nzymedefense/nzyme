package app.nzyme.core.rest.resources.system.cluster;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.Node;
import app.nzyme.core.distributed.database.metrics.GaugeHistogramBucket;
import app.nzyme.core.distributed.MetricExternalName;
import app.nzyme.core.rest.responses.nodes.NodeMetricsGaugeHistogramResponse;
import app.nzyme.core.rest.responses.nodes.NodeMetricsGaugeHistogramValueResponse;
import app.nzyme.core.rest.responses.nodes.NodeResponse;
import app.nzyme.core.rest.responses.nodes.NodesListResponse;
import app.nzyme.core.taps.metrics.BucketSize;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Path("/api/system/cluster/nodes")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class NodesResource {

    private static final Logger LOG = LogManager.getLogger(NodesResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response findAll() {
        List<NodeResponse> nodes = Lists.newArrayList();
        for (Node node : nzyme.getNodeManager().getActiveNodes()) {
            nodes.add(buildNodeResponse(node));
        }

        return Response.ok(NodesListResponse.create(nodes)).build();
    }

    @GET
    @Path("/show/{uuid}")
    public Response findOne(@PathParam("uuid") String uuid) {
        UUID nodeId;

        try {
            nodeId = UUID.fromString(uuid);
        } catch(IllegalArgumentException e) {
            LOG.warn("Invalid UUID supplied.", e);
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        Optional<Node> res = nzyme.getNodeManager().getNode(nodeId);
        if (res.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(buildNodeResponse(res.get())).build();
    }

    @GET
    @Path("/show/{uuid}/metrics/gauges/{metricname}/histogram")
    public Response findMetricsGaugeHistogram(@PathParam("uuid") String uuid, @PathParam("metricname") String n) {
        MetricExternalName metricName;
        UUID nodeId;

        try {
            metricName = MetricExternalName.valueOf(n.toUpperCase());
        } catch(IllegalArgumentException e) {
            LOG.warn("Unknown node metric name [{}].", n);
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            nodeId = UUID.fromString(uuid);
        } catch(IllegalArgumentException e) {
            LOG.warn("Invalid node ID provided.", e);
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        Optional<Map<DateTime, GaugeHistogramBucket>> histo = nzyme.getNodeManager().findMetricsHistogram(
                nodeId, metricName.database_label, 24, BucketSize.MINUTE
        );

        if (histo.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Map<DateTime, NodeMetricsGaugeHistogramValueResponse> result = Maps.newTreeMap();
        for (GaugeHistogramBucket value : histo.get().values()) {
            result.put(value.bucket(), NodeMetricsGaugeHistogramValueResponse.create(
                    value.bucket(), value.sum(), value.average(), value.maximum(), value.minimum()
            ));
        }

        return Response.ok(NodeMetricsGaugeHistogramResponse.create(result)).build();
    }

    private NodeResponse buildNodeResponse(Node node) {
        return NodeResponse.create(
                node.uuid().toString(),
                node.name(),
                node.lastSeen().isAfter(DateTime.now().minusMinutes(2)),
                node.httpExternalUri().toString(),
                node.memoryBytesTotal(),
                node.memoryBytesAvailable(),
                node.memoryBytesUsed(),
                node.heapBytesTotal(),
                node.heapBytesAvailable(),
                node.heapBytesUsed(),
                node.cpuSystemLoad(),
                node.cpuThreadCount(),
                node.processStartTime(),
                node.processVirtualSize(),
                node.processArguments(),
                node.osInformation(),
                node.version(),
                node.lastSeen()
        );
    }

}
