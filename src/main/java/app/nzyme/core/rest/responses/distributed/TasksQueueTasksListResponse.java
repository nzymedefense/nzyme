package app.nzyme.core.rest.responses.distributed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TasksQueueTasksListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("tasks")
    public abstract List<TasksQueueTaskResponse> tasks();

    public static TasksQueueTasksListResponse create(long count, List<TasksQueueTaskResponse> tasks) {
        return builder()
                .count(count)
                .tasks(tasks)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TasksQueueTasksListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder tasks(List<TasksQueueTaskResponse> tasks);

        public abstract TasksQueueTasksListResponse build();
    }

}
