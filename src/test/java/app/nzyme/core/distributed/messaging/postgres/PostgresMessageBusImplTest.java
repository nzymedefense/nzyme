package app.nzyme.core.distributed.messaging.postgres;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.Database;
import app.nzyme.plugin.distributed.messaging.*;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

public class PostgresMessageBusImplTest {

    @BeforeMethod
    public void clean() {
        NzymeNode nzyme = new MockNzyme();

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("TRUNCATE message_bus_messages")
                        .execute()
        );

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("TRUNCATE nodes")
                        .execute()
        );
    }

    @Test
    public void testSendAndPollWithSuccessfulResult() throws InterruptedException {
        NzymeNode nzyme = new MockNzyme(0, Integer.MAX_VALUE, TimeUnit.DAYS);

        final AtomicInteger counter = new AtomicInteger(0);
        nzyme.getMessageBus().onMessageReceived(MessageType.CHECK_RESTART_HTTP_SERVER, new MessageHandler() {
            @Override
            public MessageProcessingResult handle(Message message) {
                long notAckCount = nzyme.getDatabase().withHandle(handle ->
                        handle.createQuery("SELECT COUNT(*) FROM message_bus_messages WHERE status != 'ACK'")
                                .mapTo(Long.class)
                                .one()
                );
                assertEquals(notAckCount, 0);

                counter.incrementAndGet();
                return MessageProcessingResult.SUCCESS;
            }

            @Override
            public String getName() {
                return "test";
            }
        });

        assertEquals(counter.get(), 0);

        nzyme.getMessageBus().send(Message.create(
                nzyme.getNodeManager().getLocalNodeId(),
                MessageType.CHECK_RESTART_HTTP_SERVER,
                Collections.emptyMap(),
                false)
        );

        long notNewCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM message_bus_messages WHERE status != 'NEW'")
                        .mapTo(Long.class)
                        .one()
        );
        assertEquals(notNewCount, 0);

        ((PostgresMessageBusImpl) nzyme.getMessageBus()).poll();

        assertEquals(counter.get(), 1);

        long failureCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM message_bus_messages WHERE status != 'PROCESSED_SUCCESS'")
                        .mapTo(Long.class)
                        .one()
        );

        assertEquals(failureCount, 0);
    }

    @Test
    public void testSendAndPollWithFailureResult() throws InterruptedException {
        NzymeNode nzyme = new MockNzyme(0, Integer.MAX_VALUE, TimeUnit.DAYS);

        final AtomicInteger counter = new AtomicInteger(0);
        nzyme.getMessageBus().onMessageReceived(MessageType.CHECK_RESTART_HTTP_SERVER, new MessageHandler() {
            @Override
            public MessageProcessingResult handle(Message message) {
                long notAckCount = nzyme.getDatabase().withHandle(handle ->
                        handle.createQuery("SELECT COUNT(*) FROM message_bus_messages WHERE status != 'ACK'")
                                .mapTo(Long.class)
                                .one()
                );
                assertEquals(notAckCount, 0);

                counter.incrementAndGet();
                return MessageProcessingResult.FAILURE;
            }

            @Override
            public String getName() {
                return "test";
            }
        });

        assertEquals(counter.get(), 0);

        nzyme.getMessageBus().send(Message.create(
                nzyme.getNodeManager().getLocalNodeId(),
                MessageType.CHECK_RESTART_HTTP_SERVER,
                Collections.emptyMap(),
                false
        ));

        long notNewCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM message_bus_messages WHERE status != 'NEW'")
                        .mapTo(Long.class)
                        .one()
        );
        assertEquals(notNewCount, 0);

        ((PostgresMessageBusImpl) nzyme.getMessageBus()).poll();

        assertEquals(counter.get(), 1);

        long failureCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM message_bus_messages WHERE status != 'PROCESSED_FAILURE'")
                        .mapTo(Long.class)
                        .one()
        );

        assertEquals(failureCount, 0);
    }

    @Test
    public void testRetentionCleaning() {
        NzymeNode nzyme = new MockNzyme(0, Integer.MAX_VALUE, TimeUnit.DAYS);
        assertEquals(messageBusMessagesTotalCount(nzyme.getDatabase()), 0);

        nzyme.getMessageBus().send(Message.create(
                nzyme.getNodeManager().getLocalNodeId(),
                MessageType.CHECK_RESTART_HTTP_SERVER,
                Collections.emptyMap(),
                false
        ));

        nzyme.getMessageBus().send(Message.create(
                nzyme.getNodeManager().getLocalNodeId(),
                MessageType.CHECK_RESTART_HTTP_SERVER,
                Collections.emptyMap(),
                false
        ));

        nzyme.getMessageBus().send(Message.create(
                nzyme.getNodeManager().getLocalNodeId(),
                MessageType.CHECK_RESTART_HTTP_SERVER,
                Collections.emptyMap(),
                false
        ));

        assertEquals(messageBusMessagesTotalCount(nzyme.getDatabase()), 3);

        ((PostgresMessageBusImpl) nzyme.getMessageBus()).retentionClean(DateTime.now());

        assertEquals(messageBusMessagesTotalCount(nzyme.getDatabase()), 0);
    }

    @Test
    public void testSendToAllOnlineNodes() {
        UUID n1ID = UUID.randomUUID();
        NzymeNode n1 =  new MockNzyme(0, Integer.MAX_VALUE, TimeUnit.DAYS);
        n1.getNodeManager().registerSelf(n1ID);

        UUID n2ID = UUID.randomUUID();
        NzymeNode n2 =  new MockNzyme(0, Integer.MAX_VALUE, TimeUnit.DAYS);
        n2.getNodeManager().registerSelf(n2ID);

        UUID n3ID = UUID.randomUUID();
        NzymeNode n3 =  new MockNzyme(0, Integer.MAX_VALUE, TimeUnit.DAYS);
        n3.getNodeManager().registerSelf(n3ID);

        n1.getMessageBus().sendToAllOnlineNodes(ClusterMessage.create(
                MessageType.CHECK_RESTART_HTTP_SERVER,
                Collections.emptyMap(),
                false
        ));

        long notNewCount = n1.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM message_bus_messages WHERE status != 'NEW'")
                        .mapTo(Long.class)
                        .one()
        );
        assertEquals(notNewCount, 0);

        assertEquals(messageBusMessagesTotalCount(n1.getDatabase()), 3L);
        assertEquals(messageBusMessagesForNodeCount(n1.getDatabase(), n1ID), 1L);
        assertEquals(messageBusMessagesForNodeCount(n1.getDatabase(), n2ID), 1L);
        assertEquals(messageBusMessagesForNodeCount(n1.getDatabase(), n3ID), 1L);
    }

    @Test
    public void testRespectsCurrentCycle() throws InterruptedException {
        NzymeNode nzyme = new MockNzyme(0, Integer.MAX_VALUE, TimeUnit.DAYS);

        final AtomicInteger counter = new AtomicInteger(0);
        nzyme.getMessageBus().onMessageReceived(MessageType.CHECK_RESTART_HTTP_SERVER, new MessageHandler() {
            @Override
            public MessageProcessingResult handle(Message message) {
                counter.incrementAndGet();
                return MessageProcessingResult.SUCCESS;
            }

            @Override
            public String getName() {
                return "test";
            }
        });

        assertEquals(counter.get(), 0);

        nzyme.getMessageBus().send(Message.create(
                nzyme.getNodeManager().getLocalNodeId(),
                MessageType.CHECK_RESTART_HTTP_SERVER,
                Collections.emptyMap(),
                true)
        );

        long notNewCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM message_bus_messages WHERE status != 'NEW'")
                        .mapTo(Long.class)
                        .one()
        );
        assertEquals(notNewCount, 0);

        ((PostgresMessageBusImpl) nzyme.getMessageBus()).poll();

        assertEquals(counter.get(), 1);

        long failureCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM message_bus_messages WHERE status != 'PROCESSED_SUCCESS'")
                        .mapTo(Long.class)
                        .one()
        );

        assertEquals(failureCount, 0);

        // NEW CYCLE
        assertEquals(counter.get(), 1);
        nzyme.getMessageBus().send(Message.create(
                nzyme.getNodeManager().getLocalNodeId(),
                MessageType.CHECK_RESTART_HTTP_SERVER,
                Collections.emptyMap(),
                true)
        );
        assertEquals(counter.get(), 1);

        nzyme = new MockNzyme(0, Integer.MAX_VALUE, TimeUnit.DAYS);

        assertEquals(counter.get(), 1);

        ((PostgresMessageBusImpl) nzyme.getMessageBus()).poll();

        assertEquals(counter.get(), 1);
    }

    private long messageBusMessagesTotalCount(Database db) {
        return db.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM message_bus_messages")
                        .mapTo(Long.class)
                        .one()
        );
    }

    private long messageBusMessagesForNodeCount(Database db, UUID receiver) {
        return db.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM message_bus_messages WHERE receiver_node_id = :receiver")
                        .bind("receiver", receiver)
                        .mapTo(Long.class)
                        .one()
        );
    }

}