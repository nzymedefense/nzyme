package app.nzyme.core.distributed.messaging.postgres;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.messaging.*;
import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PostgresMessageBusImpl implements MessageBus {

    private static final Logger LOG = LogManager.getLogger(PostgresMessageBusImpl.class);

    private final NzymeNode nzyme;
    private final ObjectMapper om;

    private final Map<MessageType, List<MessageHandler>> messageHandlers;

    private boolean initialized;

    public PostgresMessageBusImpl(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.initialized = false;

        this.messageHandlers = Maps.newHashMap();

        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void initialize() {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("psql-bus-poller-%d")
                .build()
        ).scheduleWithFixedDelay(this::poll, 0, 5, TimeUnit.SECONDS);

        this.initialized = true;
    }

    private void poll() {
        /*
         * query: nack, for this node, since last poll ts (persist poll in nodes), NULL cycle or current cycle
         */
    }

    @Override
    public void send(Message message) {
        if (!initialized) {
            throw new RuntimeException("Message bus is not initialized.");
        }

        LOG.debug("Sending message [{}]", message);

        String parameters;
        try {
            parameters = om.writeValueAsString(message.parameters());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize message parameters.", e);
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
        LOG.debug("Registering message bus handler [{}] for type [{}]", messageHandler.getName(), type);

        if (!messageHandlers.containsKey(type)) {
            messageHandlers.put(type, Lists.newArrayList());
        }

        messageHandlers.get(type).add(messageHandler);
    }

}
