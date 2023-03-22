package app.nzyme.core.distributed.tasksqueue;

import com.google.auto.value.AutoValue;

import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class ReceivedTask {

    public abstract TaskType type();
    public abstract UUID senderNodeId();
    public abstract boolean allowProcessSelf();
    public abstract Map<String, Object> parametersMap();
    public abstract String parametersString();
    public abstract boolean allowRetry();

    public static ReceivedTask create(TaskType type, UUID senderNodeId, boolean allowProcessSelf, Map<String, Object> parametersMap, String parametersString, boolean allowRetry) {
        return builder()
                .type(type)
                .senderNodeId(senderNodeId)
                .allowProcessSelf(allowProcessSelf)
                .parametersMap(parametersMap)
                .parametersString(parametersString)
                .allowRetry(allowRetry)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReceivedTask.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(TaskType type);

        public abstract Builder senderNodeId(UUID senderNodeId);

        public abstract Builder allowProcessSelf(boolean allowProcessSelf);

        public abstract Builder parametersMap(Map<String, Object> parametersMap);

        public abstract Builder parametersString(String parametersString);

        public abstract Builder allowRetry(boolean allowRetry);

        public abstract ReceivedTask build();
    }

}
