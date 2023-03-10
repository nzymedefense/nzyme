package app.nzyme.core.distributed.messaging.postgres;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class PostgresMessageEntry {

    public abstract long id();
    public abstract UUID sender();
    public abstract UUID receiver();
    public abstract String type();

    @Nullable
    public abstract String parameters();

    public abstract String status();

    @Nullable
    public abstract Long cycleLimiter();

    public abstract DateTime createdAt();

    @Nullable
    public abstract DateTime acknowledgedAt();

    public static PostgresMessageEntry create(long id, UUID sender, UUID receiver, String type, String parameters, String status, long cycleLimiter, DateTime createdAt, DateTime acknowledgedAt) {
        return builder()
                .id(id)
                .sender(sender)
                .receiver(receiver)
                .type(type)
                .parameters(parameters)
                .status(status)
                .cycleLimiter(cycleLimiter)
                .createdAt(createdAt)
                .acknowledgedAt(acknowledgedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PostgresMessageEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder sender(UUID sender);

        public abstract Builder receiver(UUID receiver);

        public abstract Builder type(String type);

        public abstract Builder parameters(String parameters);

        public abstract Builder status(String status);

        public abstract Builder cycleLimiter(long cycleLimiter);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder acknowledgedAt(DateTime acknowledgedAt);

        public abstract PostgresMessageEntry build();
    }

}
