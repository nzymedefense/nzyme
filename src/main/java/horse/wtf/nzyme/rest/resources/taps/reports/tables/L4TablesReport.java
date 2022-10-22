package horse.wtf.nzyme.rest.resources.taps.reports.tables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.rest.resources.taps.reports.tables.retro.l4.L4RetroPairReport;

import java.util.List;

@AutoValue
public abstract class L4TablesReport {

    public abstract List<L4RetroPairReport> retroPairs();

    @JsonCreator
    public static L4TablesReport create(@JsonProperty("retro_pairs") List<L4RetroPairReport> retroPairs) {
        return builder()
                .retroPairs(retroPairs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_L4TablesReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder retroPairs(List<L4RetroPairReport> retroPairs);

        public abstract L4TablesReport build();
    }

}
