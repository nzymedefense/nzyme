package app.nzyme.core.rest.resources.system.cluster;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.Node;
import app.nzyme.core.rest.responses.system.NodeResponse;
import app.nzyme.core.rest.responses.system.NodesListResponse;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/system/cluster/nodes")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class NodesResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response all() {
        List<NodeResponse> nodes = Lists.newArrayList();
        for (Node node : nzyme.getNodeManager().getActiveNodes()) {
            nodes.add(NodeResponse.create(
                    node.uuid().toString(),
                    node.name(),
                    node.transportAddress().toString(),
                    node.version(),
                    node.lastSeen()
            ));
        }

        return Response.ok(NodesListResponse.create(nodes)).build();
    }

}
