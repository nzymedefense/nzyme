package app.nzyme.core.distributed.messaging;

import com.google.auto.value.AutoValue;

import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class Message {

    public abstract UUID receiver();
    public abstract MessageType type();
    public abstract Map<String, Object> parameters();
    public abstract boolean limitToCurrentCycle();

    public static Message create(UUID receiver, MessageType type, Map<String, Object> parameters, boolean limitToCurrentCycle) {
        return builder()
                .receiver(receiver)
                .type(type)
                .parameters(parameters)
                .limitToCurrentCycle(limitToCurrentCycle)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Message.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder receiver(UUID receiver);

        public abstract Builder type(MessageType type);

        public abstract Builder parameters(Map<String, Object> parameters);

        public abstract Builder limitToCurrentCycle(boolean limitToCurrentCycle);

        public abstract Message build();
    }

}
