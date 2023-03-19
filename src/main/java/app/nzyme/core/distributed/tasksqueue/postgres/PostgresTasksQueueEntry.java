package app.nzyme.core.distributed.tasksqueue.postgres;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class PostgresTasksQueueEntry {

    public abstract long id();
    public abstract UUID senderNodeId();
    public abstract String type();
    public abstract boolean allowRetry();
    public abstract String parameters();
    public abstract DateTime createdAt();
    public abstract String status();
    public abstract int retries();
    public abstract int processingTimeMs();
    public abstract DateTime firstProcessedAt();
    public abstract DateTime lastProcessedAt();
    public abstract boolean allowProcessSelf();

    public static PostgresTasksQueueEntry create(long id, UUID senderNodeId, String type, boolean allowRetry, String parameters, DateTime createdAt, String status, int retries, int processingTimeMs, DateTime firstProcessedAt, DateTime lastProcessedAt, boolean allowProcessSelf) {
        return builder()
                .id(id)
                .senderNodeId(senderNodeId)
                .type(type)
                .allowRetry(allowRetry)
                .parameters(parameters)
                .createdAt(createdAt)
                .status(status)
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
        public abstract Builder id(long id);

        public abstract Builder senderNodeId(UUID senderNodeId);

        public abstract Builder type(String type);

        public abstract Builder allowRetry(boolean allowRetry);

        public abstract Builder parameters(String parameters);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder status(String status);

        public abstract Builder retries(int retries);

        public abstract Builder processingTimeMs(int processingTimeMs);

        public abstract Builder firstProcessedAt(DateTime firstProcessedAt);

        public abstract Builder lastProcessedAt(DateTime lastProcessedAt);

        public abstract Builder allowProcessSelf(boolean allowProcessSelf);

        public abstract PostgresTasksQueueEntry build();
    }
}
