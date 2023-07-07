package app.nzyme.core.rest.resources.taps.reports.tables.dot11;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class Dot11TablesReport {

    public abstract Map<String, Dot11BSSIDReport> bssids();
    public abstract Map<String, Dot11ClientReport> clients();

    @JsonCreator
    public static Dot11TablesReport create(@JsonProperty("bssids") Map<String, Dot11BSSIDReport> bssids,
                                           @JsonProperty("clients") Map<String, Dot11ClientReport> clients) {
        return builder()
                .bssids(bssids)
                .clients(clients)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11TablesReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssids(Map<String, Dot11BSSIDReport> bssids);

        public abstract Builder clients(Map<String, Dot11ClientReport> clients);

        public abstract Dot11TablesReport build();
    }
}
