package app.nzyme.core.distributed.tasksqueue;

import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class Task {

    public abstract TaskType type();
    public abstract boolean allowProcessSelf();
    public abstract Map<String, Object> parameters();
    public abstract boolean allowRetry();

    public static Task create(TaskType type, boolean allowProcessSelf, Map<String, Object> parameters, boolean allowRetry) {
        return builder()
                .type(type)
                .allowProcessSelf(allowProcessSelf)
                .parameters(parameters)
                .allowRetry(allowRetry)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Task.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(TaskType type);

        public abstract Builder allowProcessSelf(boolean allowProcessSelf);

        public abstract Builder parameters(Map<String, Object> parameters);

        public abstract Builder allowRetry(boolean allowRetry);

        public abstract Task build();
    }
}
