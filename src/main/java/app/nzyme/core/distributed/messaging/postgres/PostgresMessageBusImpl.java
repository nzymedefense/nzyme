package app.nzyme.core.distributed.messaging.postgres;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.Node;
import app.nzyme.core.distributed.messaging.*;
import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.HashMap;
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

        this.messageHandlers = Maps.newConcurrentMap();

        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void initialize() {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("psql-bus-poller-%d")
                .build()
        ).scheduleWithFixedDelay(this::poll, 0, 5, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("psql-retention-cleaner-%d")
                .build()
        ).scheduleAtFixedRate(this::retentionClean, 0, 1, TimeUnit.HOURS);

        this.initialized = true;
    }

    private void poll() {
        try {
            List<PostgresMessageEntry> messages = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT * FROM message_bus_messages WHERE receiver_node_id = :local_node_id " +
                                    "AND status = 'NEW' AND (cycle_limiter IS NULL OR cycle_limiter = :local_node_cycle) " +
                                    "ORDER BY created_at ASC")
                            .bind("local_node_id", nzyme.getNodeManager().getLocalNodeId())
                            .bind("local_node_cycle", nzyme.getNodeManager().getLocalCycle())
                            .mapTo(PostgresMessageEntry.class)
                            .list()
            );

            LOG.debug("Polled <{}> messages from message bus.", messages.size());

            for (PostgresMessageEntry message : messages) {
                LOG.debug("Polled message from bus: [{}]", message);

                // Acknowledge message.
                setMessageStatus(message.id(), MessageStatus.ACK);

                MessageType type;
                try {
                    type = MessageType.valueOf(message.type());
                } catch(IllegalArgumentException e) {
                    LOG.warn("Unsupported message type [{}]. Skipping.", message.type());
                    continue;
                }

                // Send to registered handlers.
                if (messageHandlers.containsKey(type)) {
                    for (MessageHandler handler : messageHandlers.get(type)) {
                        Map<String, Object> parameters = this.om.readValue(
                                message.parameters(),
                                new TypeReference<HashMap<String,Object>>() {}
                        );

                        MessageProcessingResult opResult = handler.handle(Message.create(
                                message.receiver(),
                                type,
                                parameters,
                                message.cycleLimiter() != null
                        ));

                        setMessageStatus(message.id(),
                                opResult == MessageProcessingResult.SUCCESS
                                        ? MessageStatus.PROCESSED_SUCCESS : MessageStatus.PROCESSED_FAILURE
                        );
                    }
                }
            }
        } catch(Exception e) {
            LOG.error("Could not poll message bus.", e);
        }
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

        long currentCycleOfReceiver = nzyme.getNodeManager().getCycleOfNode(message.receiver());

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO message_bus_messages(sender_node_id, receiver_node_id, type, " +
                                "parameters, status, cycle_limiter, created_at, acknowledged_at) VALUES(:sender_node_id, " +
                                ":receiver_node_id, :type, :parameters, :status, :cycle_limiter, :created_at, NULL)")
                        .bind("sender_node_id", nzyme.getNodeInformation().id())
                        .bind("receiver_node_id", message.receiver())
                        .bind("type", message.type())
                        .bind("parameters", parameters)
                        .bind("status", MessageStatus.NEW)
                        .bind("cycle_limiter", currentCycleOfReceiver)
                        .bind("created_at", DateTime.now())
                        .execute()
        );
    }

    @Override
    public void sendToAllOnlineNodes(ClusterMessage message) {
        List<Node> targets = Lists.newArrayList();
        for (Node node : nzyme.getNodeManager().getNodes()) {
            // Only send to currently active nodes.
            if (node.lastSeen().isAfter(DateTime.now().minusSeconds(30))) {
                targets.add(node);
            }
        }

        for (Node target : targets) {
            send(Message.create(
                    target.uuid(),
                    message.type(),
                    message.parameters(),
                    message.limitToCurrentCycle()
            ));
        }
    }

    @Override
    public void onMessageReceived(MessageType type, MessageHandler messageHandler) {
        LOG.debug("Registering message bus handler [{}] for type [{}]", messageHandler.getName(), type);

        if (!messageHandlers.containsKey(type)) {
            messageHandlers.put(type, Lists.newArrayList());
        }

        messageHandlers.get(type).add(messageHandler);
    }

    private void setMessageStatus(long messageId, MessageStatus status) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE message_bus_messages SET status = :status WHERE id = :id")
                        .bind("status", status.name())
                        .bind("id", messageId)
                        .execute()
        );
    }

    public void retentionClean() {
        LOG.info("Running retention cleaning for message bus.");
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM message_bus_messages WHERE created_at < :timeout")
                        .bind("timeout", DateTime.now().minusDays(7))
                        .execute()
        );
    }

}
