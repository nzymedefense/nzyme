package app.nzyme.core.distributed.messaging.postgres;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.distributed.messaging.Message;
import app.nzyme.plugin.distributed.messaging.MessageHandler;
import app.nzyme.plugin.distributed.messaging.MessageProcessingResult;
import app.nzyme.plugin.distributed.messaging.MessageType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
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
    }

    @Test
    public void testSendAndPollWithSuccessfulResult() throws InterruptedException {
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
                handle.createQuery("SELECT COUNT(*) FROM message_bus_messages WHERE status != 'PROCESSED_FAILURE'")
                        .mapTo(Long.class)
                        .one()
        );

        assertEquals(failureCount, 0);
    }

}