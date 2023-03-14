package app.nzyme.core.distributed.messaging;

import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class ClusterMessage {

    public abstract MessageType type();
    public abstract Map<String, Object> parameters();
    public abstract boolean limitToCurrentCycle();

    public static ClusterMessage create(MessageType type, Map<String, Object> parameters, boolean limitToCurrentCycle) {
        return builder()
                .type(type)
                .parameters(parameters)
                .limitToCurrentCycle(limitToCurrentCycle)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClusterMessage.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(MessageType type);

        public abstract Builder parameters(Map<String, Object> parameters);

        public abstract Builder limitToCurrentCycle(boolean limitToCurrentCycle);

        public abstract ClusterMessage build();
    }
    
}
