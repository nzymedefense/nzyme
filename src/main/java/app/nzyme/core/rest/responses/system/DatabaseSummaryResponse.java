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

    @JsonProperty("ethernet_l4_retention_time_days")
    public abstract ConfigurationEntryResponse ethernetL4RetentionTimeDays();

    @JsonProperty("ethernet_dns_retention_time_days")
    public abstract ConfigurationEntryResponse ethernetDnsRetentionTimeDays();

    @JsonProperty("ethernet_arp_retention_time_days")
    public abstract ConfigurationEntryResponse ethernetArpRetentionTimeDays();

    public static DatabaseSummaryResponse create(long totalSize, long ethernetSize, long dot11Size, ConfigurationEntryResponse dot11RetentionTimeDays, ConfigurationEntryResponse ethernetL4RetentionTimeDays, ConfigurationEntryResponse ethernetDnsRetentionTimeDays, ConfigurationEntryResponse ethernetArpRetentionTimeDays) {
        return builder()
                .totalSize(totalSize)
                .ethernetSize(ethernetSize)
                .dot11Size(dot11Size)
                .dot11RetentionTimeDays(dot11RetentionTimeDays)
                .ethernetL4RetentionTimeDays(ethernetL4RetentionTimeDays)
                .ethernetDnsRetentionTimeDays(ethernetDnsRetentionTimeDays)
                .ethernetArpRetentionTimeDays(ethernetArpRetentionTimeDays)
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

        public abstract Builder ethernetL4RetentionTimeDays(ConfigurationEntryResponse ethernetL4RetentionTimeDays);

        public abstract Builder ethernetDnsRetentionTimeDays(ConfigurationEntryResponse ethernetDnsRetentionTimeDays);

        public abstract Builder ethernetArpRetentionTimeDays(ConfigurationEntryResponse ethernetArpRetentionTimeDays);

        public abstract DatabaseSummaryResponse build();
    }
}
