package app.nzyme.core.rest.resources.taps.reports.tables.dot11;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Dot11SecurityInformationReport {

    public abstract List<String> protocols();
    public abstract Dot11CipherSuitesReport suites();

    @JsonCreator
    public static Dot11SecurityInformationReport create(@JsonProperty("protocols") List<String> protocols,
                                                        @JsonProperty("suites") Dot11CipherSuitesReport suites) {
        return builder()
                .protocols(protocols)
                .suites(suites)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11SecurityInformationReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder protocols(List<String> protocols);

        public abstract Builder suites(Dot11CipherSuitesReport suites);

        public abstract Dot11SecurityInformationReport build();
    }
}
