package app.nzyme.core.rest.resources.taps.reports.tables.tls;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TlsAlertReport {

    public abstract String direction();
    public abstract int level();
    public abstract int description();
    public abstract String descriptionName();
    public abstract boolean encrypted();

    @JsonCreator
    public static TlsAlertReport create(@JsonProperty("direction") String direction,
                                        @JsonProperty("level") int level,
                                        @JsonProperty("description") int description,
                                        @JsonProperty("description_name") String descriptionName,
                                        @JsonProperty("encrypted") boolean encrypted) {
        return builder()
                .direction(direction)
                .level(level)
                .description(description)
                .descriptionName(descriptionName)
                .encrypted(encrypted)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TlsAlertReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder direction(String direction);

        public abstract Builder level(int level);

        public abstract Builder description(int description);

        public abstract Builder descriptionName(String descriptionName);
        public abstract Builder encrypted(boolean encrypted);

        public abstract TlsAlertReport build();
    }
}
