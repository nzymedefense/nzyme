package app.nzyme.core.events.types;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class SystemEvent {

    public abstract SystemEventType type();
    public abstract DateTime timestamp();
    public abstract String details();

    public static SystemEvent create(SystemEventType type, DateTime timestamp, String details) {
        return builder()
                .type(type)
                .timestamp(timestamp)
                .details(details)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SystemEvent.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(SystemEventType type);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder details(String details);

        public abstract SystemEvent build();
    }
}
