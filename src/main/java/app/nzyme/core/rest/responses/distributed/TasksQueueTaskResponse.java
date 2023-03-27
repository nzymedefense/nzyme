package app.nzyme.core.rest.responses.distributed;

import app.nzyme.plugin.distributed.tasksqueue.TaskStatus;
import app.nzyme.plugin.distributed.tasksqueue.TaskType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class TasksQueueTaskResponse {

    @JsonProperty("id")
    public abstract Long id();

    @JsonProperty("sender")
    public abstract UUID sender();

    @JsonProperty("type")
    public abstract TaskType type();

    @JsonProperty("allow_retry")
    public abstract Boolean allowRetry();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("status")
    public abstract TaskStatus status();

    @JsonProperty("retries")
    public abstract Integer retries();

    @JsonProperty("allow_process_self")
    public abstract boolean allowProcessSelf();

    @JsonProperty("processing_time_ms")
    @Nullable
    public abstract Integer processingTimeMs();

    @JsonProperty("first_processed_at")
    @Nullable
    public abstract DateTime firstProcessedAt();

    @JsonProperty("last_processed_at")
    @Nullable
    public abstract DateTime lastProcessedAt();

    @JsonProperty("processed_by")
    @Nullable
    public abstract UUID processedBy();

    public static TasksQueueTaskResponse create(Long id, UUID sender, TaskType type, Boolean allowRetry, DateTime createdAt, TaskStatus status, Integer retries, boolean allowProcessSelf, Integer processingTimeMs, DateTime firstProcessedAt, DateTime lastProcessedAt, UUID processedBy) {
        return builder()
                .id(id)
                .sender(sender)
                .type(type)
                .allowRetry(allowRetry)
                .createdAt(createdAt)
                .status(status)
                .retries(retries)
                .allowProcessSelf(allowProcessSelf)
                .processingTimeMs(processingTimeMs)
                .firstProcessedAt(firstProcessedAt)
                .lastProcessedAt(lastProcessedAt)
                .processedBy(processedBy)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TasksQueueTaskResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder sender(UUID sender);

        public abstract Builder type(TaskType type);

        public abstract Builder allowRetry(Boolean allowRetry);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder status(TaskStatus status);

        public abstract Builder retries(Integer retries);

        public abstract Builder allowProcessSelf(boolean allowProcessSelf);

        public abstract Builder processingTimeMs(Integer processingTimeMs);

        public abstract Builder firstProcessedAt(DateTime firstProcessedAt);

        public abstract Builder lastProcessedAt(DateTime lastProcessedAt);

        public abstract Builder processedBy(UUID processedBy);

        public abstract TasksQueueTaskResponse build();
    }
}
