package app.nzyme.core.rest.responses.system;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DatabaseSummaryResponse {

    @JsonProperty("total_size")
    public abstract long totalSize();

    @JsonProperty("ethernet_size")
    public abstract long ethernetSize();

    @JsonProperty("dot11_size")
    public abstract long dot11Size();

    @JsonProperty("dot11_retention_time_days")
    public abstract ConfigurationEntryResponse dot11RetentionTimeDays();

    public static DatabaseSummaryResponse create(long totalSize, long ethernetSize, long dot11Size, ConfigurationEntryResponse dot11RetentionTimeDays) {
        return builder()
                .totalSize(totalSize)
                .ethernetSize(ethernetSize)
                .dot11Size(dot11Size)
                .dot11RetentionTimeDays(dot11RetentionTimeDays)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DatabaseSummaryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder totalSize(long totalSize);

        public abstract Builder ethernetSize(long ethernetSize);

        public abstract Builder dot11Size(long dot11Size);

        public abstract Builder dot11RetentionTimeDays(ConfigurationEntryResponse dot11RetentionTimeDays);

        public abstract DatabaseSummaryResponse build();
    }
}
