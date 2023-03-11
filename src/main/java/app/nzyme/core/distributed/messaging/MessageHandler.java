package app.nzyme.core.distributed.messaging;

public interface MessageHandler {

    MessageProcessingResult handle(Message message);
    String getName();

}
