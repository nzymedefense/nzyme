package app.nzyme.core.rest.responses.distributed;

import app.nzyme.plugin.distributed.messaging.MessageStatus;
import app.nzyme.plugin.distributed.messaging.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class MessageBusMessageResponse {

    @JsonProperty("id")
    public abstract Long id();

    @JsonProperty("sender")
    public abstract UUID sender();

    @JsonProperty("receiver")
    public abstract UUID receiver();

    @JsonProperty("type")
    public abstract MessageType type();

    @JsonProperty("status")
    public abstract MessageStatus status();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("cycle_limiter")
    @Nullable
    public abstract Long cycleLimiter();

    @JsonProperty("acknowledged_at")
    @Nullable
    public abstract DateTime acknowledgedAt();

    public static MessageBusMessageResponse create(Long id, UUID sender, UUID receiver, MessageType type, MessageStatus status, DateTime createdAt, Long cycleLimiter, DateTime acknowledgedAt) {
        return builder()
                .id(id)
                .sender(sender)
                .receiver(receiver)
                .type(type)
                .status(status)
                .createdAt(createdAt)
                .cycleLimiter(cycleLimiter)
                .acknowledgedAt(acknowledgedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MessageBusMessageResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder sender(UUID sender);

        public abstract Builder receiver(UUID receiver);

        public abstract Builder type(MessageType type);

        public abstract Builder status(MessageStatus status);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder cycleLimiter(Long cycleLimiter);

        public abstract Builder acknowledgedAt(DateTime acknowledgedAt);

        public abstract MessageBusMessageResponse build();
    }
}
