package app.nzyme.core.distributed.tasksqueue.postgres;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class PostgresTasksQueueEntry {

    public abstract Long id();
    public abstract UUID senderNodeId();
    public abstract String type();
    public abstract Boolean allowRetry();
    public abstract String parameters();
    public abstract DateTime createdAt();
    public abstract String status();

    @Nullable
    public abstract String previousStatus();

    public abstract Integer retries();

    @Nullable
    public abstract Integer processingTimeMs();

    @Nullable
    public abstract DateTime firstProcessedAt();

    @Nullable
    public abstract DateTime lastProcessedAt();

    public abstract Boolean allowProcessSelf();

    public static PostgresTasksQueueEntry create(Long id, UUID senderNodeId, String type, Boolean allowRetry, String parameters, DateTime createdAt, String status, String previousStatus, Integer retries, Integer processingTimeMs, DateTime firstProcessedAt, DateTime lastProcessedAt, Boolean allowProcessSelf) {
        return builder()
                .id(id)
                .senderNodeId(senderNodeId)
                .type(type)
                .allowRetry(allowRetry)
                .parameters(parameters)
                .createdAt(createdAt)
                .status(status)
                .previousStatus(previousStatus)
                .retries(retries)
                .processingTimeMs(processingTimeMs)
                .firstProcessedAt(firstProcessedAt)
                .lastProcessedAt(lastProcessedAt)
                .allowProcessSelf(allowProcessSelf)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PostgresTasksQueueEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder senderNodeId(UUID senderNodeId);

        public abstract Builder type(String type);

        public abstract Builder allowRetry(Boolean allowRetry);

        public abstract Builder parameters(String parameters);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder status(String status);

        public abstract Builder previousStatus(String previousStatus);

        public abstract Builder retries(Integer retries);

        public abstract Builder processingTimeMs(Integer processingTimeMs);

        public abstract Builder firstProcessedAt(DateTime firstProcessedAt);

        public abstract Builder lastProcessedAt(DateTime lastProcessedAt);

        public abstract Builder allowProcessSelf(Boolean allowProcessSelf);

        public abstract PostgresTasksQueueEntry build();
    }
}
