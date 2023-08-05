package app.nzyme.core.detection.alerts.db;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DetectionAlertAttributeEntry {

    public abstract long id();
    public abstract long detectionAlertId();
    public abstract String key();
    public abstract String value();

    public static DetectionAlertAttributeEntry create(long id, long detectionAlertId, String key, String value) {
        return builder()
                .id(id)
                .detectionAlertId(detectionAlertId)
                .key(key)
                .value(value)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DetectionAlertAttributeEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder detectionAlertId(long detectionAlertId);

        public abstract Builder key(String key);

        public abstract Builder value(String value);

        public abstract DetectionAlertAttributeEntry build();
    }
}
