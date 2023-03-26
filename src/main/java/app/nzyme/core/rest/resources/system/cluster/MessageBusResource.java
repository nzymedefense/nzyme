package app.nzyme.core.rest.resources.system.cluster;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.responses.distributed.MessageBusMessageListResponse;
import app.nzyme.core.rest.responses.distributed.MessageBusMessageResponse;
import app.nzyme.plugin.distributed.messaging.StoredMessage;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/system/cluster/messagebus")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class MessageBusResource {

    private static final Logger LOG = LogManager.getLogger(MessageBusResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("messages")
    public Response findMessages(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        if (limit > 250) {
            LOG.warn("Requested limit larger than 250. Not allowed.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<MessageBusMessageResponse> responseMessages = Lists.newArrayList();

        for (StoredMessage message : nzyme.getMessageBus().getAllMessages(limit, offset)) {
            responseMessages.add(MessageBusMessageResponse.create(
                    message.id(),
                    message.sender(),
                    message.receiver(),
                    message.type(),
                    message.status(),
                    message.createdAt(),
                    message.cycleLimiter(),
                    message.acknowledgedAt()
            ));
        }

        long count = nzyme.getMessageBus().getTotalMessageCount();

        return Response.ok(MessageBusMessageListResponse.create(count, responseMessages)).build();
    }

}
