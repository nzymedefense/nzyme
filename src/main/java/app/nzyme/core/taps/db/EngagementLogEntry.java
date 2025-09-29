package app.nzyme.core.taps.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class EngagementLogEntry {

    public abstract long id();
    public abstract String message();
    public abstract UUID tapUuid();
    public abstract DateTime timestamp();

    public static EngagementLogEntry create(long id, String message, UUID tapUuid, DateTime timestamp) {
        return builder()
                .id(id)
                .message(message)
                .tapUuid(tapUuid)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EngagementLogEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder message(String message);

        public abstract Builder tapUuid(UUID tapUuid);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract EngagementLogEntry build();
    }
}
