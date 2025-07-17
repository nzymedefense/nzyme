package app.nzyme.core.rest.resources.taps.reports.tables.arp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ArpPacketsReport {

    public abstract List<ArpPacketReport> packets();

    @JsonCreator
    public static ArpPacketsReport create(@JsonProperty("packets") List<ArpPacketReport> packets) {
        return builder()
                .packets(packets)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ArpPacketsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder packets(List<ArpPacketReport> packets);

        public abstract ArpPacketsReport build();
    }
}
