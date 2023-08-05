package app.nzyme.core.rest.resources.taps.reports.tables.dot11;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class Dot11AlertReport {

    public enum AlertType {
        PwnagotchiDetected
    }

    public enum AlertAttributeType {
        Number,
        String
    }

    public abstract AlertType alertType();
    public abstract Map<String, Map<AlertAttributeType, Object>> attributes();

    @JsonCreator
    public static Dot11AlertReport create(@JsonProperty("alert_type") AlertType alertType,
                                          @JsonProperty("attributes") Map<String, Map<AlertAttributeType, Object>> attributes) {
        return builder()
                .alertType(alertType)
                .attributes(attributes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11AlertReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder alertType(AlertType alertType);

        public abstract Builder attributes(Map<String, Map<AlertAttributeType, Object>> attributes);

        public abstract Dot11AlertReport build();
    }
}
