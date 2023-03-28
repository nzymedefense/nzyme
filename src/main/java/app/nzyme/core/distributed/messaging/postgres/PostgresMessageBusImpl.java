package app.nzyme.core.distributed.messaging.postgres;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.distributed.Node;
import app.nzyme.plugin.distributed.messaging.*;
import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
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

    public void initialize(int pollInterval, TimeUnit pollIntervalUnit) {
        // Find existing still ACK'd messages of this node and mark as failed. They were stuck/running at last shutdown.
        nzyme.getDatabase().withHandle(handle ->
                handle.createUpdate("UPDATE message_bus_messages SET status = :failed " +
                                "WHERE status = :acked AND acknowledged_by = :node_id")
                        .bind("acked", MessageStatus.ACK)
                        .bind("failed", MessageStatus.PROCESSED_FAILURE)
                        .bind("node_id", nzyme.getNodeInformation().id())
                        .execute()
        );

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("psql-bus-poller-%d")
                .build()
        ).scheduleWithFixedDelay(this::poll, pollInterval, pollInterval, pollIntervalUnit);

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("psql-bus-retention-cleaner-%d")
                .build()
        ).scheduleAtFixedRate(() -> retentionClean(DateTime.now().minusDays(7)),
                1, 1, TimeUnit.HOURS);

        this.initialized = true;
    }

    @Override
    public void initialize() {
        initialize(5, TimeUnit.SECONDS);
    }

    public void poll() {
        try {
            List<PostgresMessageEntry> messages = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT * FROM message_bus_messages WHERE receiver_node_id = :local_node_id " +
                                    "AND status = :status AND (cycle_limiter IS NULL OR cycle_limiter = :local_node_cycle) " +
                                    "ORDER BY created_at ASC")
                            .bind("status", MessageStatus.NEW)
                            .bind("local_node_id", nzyme.getNodeManager().getLocalNodeId())
                            .bind("local_node_cycle", nzyme.getNodeManager().getLocalCycle())
                            .mapTo(PostgresMessageEntry.class)
                            .list()
            );

            LOG.debug("Polled <{}> messages from message bus.", messages.size());

            for (PostgresMessageEntry message : messages) {
                LOG.debug("Polled message from bus: [{}]", message);

                // Acknowledge message.
                ackMessage(message.id());

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
                        Map<String, Object> serializedParameters = this.om.readValue(
                                message.parameters(),
                                new TypeReference<HashMap<String,Object>>() {}
                        );

                        Stopwatch stopwatch = Stopwatch.createStarted();
                        MessageProcessingResult opResult = handler.handle(ReceivedMessage.create(
                                message.receiver(),
                                message.sender(),
                                type,
                                serializedParameters,
                                message.parameters(),
                                message.cycleLimiter() != null
                        ));
                        long tookMs = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                        if (tookMs == 0) {
                            tookMs = 1;
                        }

                        if (opResult.equals(MessageProcessingResult.FAILURE)) {
                            LOG.error("Could not handle cluster message <#{}> of type [{}]. Marking as failure.",
                                    message.id(), message.type());
                        }

                        setMessageStatus(message.id(),
                                opResult == MessageProcessingResult.SUCCESS
                                        ? MessageStatus.PROCESSED_SUCCESS : MessageStatus.PROCESSED_FAILURE
                        );

                        setMessagePostProcessMetadata(message.id(), (int) tookMs);
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
                                "parameters, status, cycle_limiter, created_at) VALUES(:sender_node_id, " +
                                ":receiver_node_id, :type, :parameters, :status, :cycle_limiter, :created_at)")
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

    @Override
    public void acknowledgeMessageFailure(long messageId) {
        setMessageStatus(messageId, MessageStatus.FAILURE_ACKNOWLEDGED);
    }

    @Override
    public void acknowledgeAllMessageFailures() {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE message_bus_messages SET status = :acked WHERE status = :failed")
                        .bind("acked", MessageStatus.FAILURE_ACKNOWLEDGED)
                        .bind("failed", MessageStatus.PROCESSED_FAILURE)
                        .execute()
        );
    }

    @Override
    public List<StoredMessage> getAllFailedMessagesSince(DateTime since) {
        List<PostgresMessageEntry> failures = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM message_bus_messages WHERE status = :status AND created_at > :since " +
                                "ORDER BY created_at DESC")
                        .bind("status", MessageStatus.PROCESSED_FAILURE)
                        .bind("since", since)
                        .mapTo(PostgresMessageEntry.class)
                        .list()
        );


        return entriesToStoredMessages(failures);
    }

    @Override
    public List<StoredMessage> getAllStuckMessages(DateTime timeout) {
        List<PostgresMessageEntry> failures = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM message_bus_messages WHERE status = :status AND acknowledged_at < :timeout " +
                                "ORDER BY created_at DESC")
                        .bind("status", MessageStatus.ACK)
                        .bind("timeout", timeout)
                        .mapTo(PostgresMessageEntry.class)
                        .list()
        );

        return entriesToStoredMessages(failures);
    }

    @Override
    public List<StoredMessage> getAllMessages(int limit, int offset) {
        List<PostgresMessageEntry> entries = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM message_bus_messages ORDER BY created_at DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(PostgresMessageEntry.class)
                        .list()
        );

        return entriesToStoredMessages(entries);
    }

    @Override
    public long getTotalMessageCount() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM message_bus_messages")
                        .mapTo(Long.class)
                        .one()
        );
    }

    public void retentionClean(DateTime cutoff) {
        LOG.info("Running retention cleaning for message bus.");
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM message_bus_messages WHERE created_at < :timeout")
                        .bind("timeout", cutoff)
                        .execute()
        );
    }

    private void setMessageStatus(long messageId, MessageStatus status) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE message_bus_messages SET status = :status WHERE id = :id")
                        .bind("status", status.name())
                        .bind("id", messageId)
                        .execute()
        );
    }

    private void ackMessage(long messageId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE message_bus_messages SET status = :status, " +
                                "acknowledged_at = :acknowledged_at, acknowledged_by = :node_id WHERE id = :id")
                        .bind("status", MessageStatus.ACK.name())
                        .bind("node_id", nzyme.getNodeInformation().id())
                        .bind("id", messageId)
                        .bind("acknowledged_at", DateTime.now())
                        .execute()
        );
    }

    private void setMessagePostProcessMetadata(long id, int tookMs) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE message_bus_messages SET processing_time_ms = :took_ms " +
                                "WHERE id = :id")
                        .bind("took_ms", tookMs)
                        .bind("id", id)
                        .execute()
        );
    }

    private List<StoredMessage> entriesToStoredMessages(List<PostgresMessageEntry> failures) {
        List<StoredMessage> result = Lists.newArrayList();

        for (PostgresMessageEntry failure : failures) {
            Map<String, Object> serializedParameters;
            try {
                serializedParameters = this.om.readValue(
                        failure.parameters(),
                        new TypeReference<HashMap<String,Object>>() {}
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not serialize parameters of message <" + failure.id() + ">.", e);
            }

            result.add(StoredMessage.create(
                    failure.id(),
                    failure.sender(),
                    failure.receiver(),
                    MessageType.valueOf(failure.type()),
                    serializedParameters,
                    MessageStatus.valueOf(failure.status()),
                    failure.createdAt(),
                    failure.cycleLimiter(),
                    failure.acknowledgedAt(),
                    failure.processingTimeMs()
            ));
        }

        return result;
    }

}
