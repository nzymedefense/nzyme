package app.nzyme.core.distributed.messaging;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class Message {

    public abstract UUID receiver();
    public abstract MessageType type();

    @Nullable
    public abstract Map<String, Object> parameters();

    @Nullable
    public abstract Long cycleLimiter();

    public abstract DateTime createdAt();

    @Nullable
    public abstract DateTime acknowledgedAt();

    public static Message create(UUID receiver, MessageType type, Map<String, Object> parameters, long cycleLimiter, DateTime createdAt, DateTime acknowledgedAt) {
        return builder()
                .receiver(receiver)
                .type(type)
                .parameters(parameters)
                .cycleLimiter(cycleLimiter)
                .createdAt(createdAt)
                .acknowledgedAt(acknowledgedAt)
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

        public abstract Builder cycleLimiter(long cycleLimiter);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder acknowledgedAt(DateTime acknowledgedAt);

        public abstract Message build();
    }
}
