package app.nzyme.core.distributed.messaging.postgres;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.messaging.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class PostgresMessageBusImpl implements MessageBus {

    private static final Logger LOG = LogManager.getLogger(PostgresMessageBusImpl.class);

    private final NzymeNode nzyme;
    private final ObjectMapper om;

    public PostgresMessageBusImpl(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void send(Message message) {
        LOG.debug("Sending message [{}]", message);

        String parameters;
        try {
            parameters = om.writeValueAsString(message.parameters());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize message.", e);
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO message_bus_messages(sender_node_id, receiver_node_id, type, " +
                                "parameters, status, cycle_limiter, created_at, acknowledged_at) VALUES(:sender_node_id, " +
                                ":receiver_node_id, :type, :parameters, :status, :cycle_limiter, :created_at, NULL)")
                        .bind("sender_node_id", nzyme.getNodeInformation().id())
                        .bind("receiver_node_id", message.receiver())
                        .bind("type", message.type())
                        .bind("parameters", parameters)
                        .bind("status", MessageStatus.NEW)
                        .bind("cycle_limiter", message.cycleLimiter())
                        .bind("created_at", DateTime.now())
                        .execute()
        );
    }

    @Override
    public void onMessageReceived(MessageType type, MessageHandler messageHandler) {

    }

}
