package app.nzyme.core.rest.resources.taps.reports.context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TapContextReport {

    public abstract List<TapMacContextReport> macs();

    @JsonCreator
    public static TapContextReport create(@JsonProperty("macs") List<TapMacContextReport> macs) {
        return builder()
                .macs(macs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapContextReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder macs(List<TapMacContextReport> macs);

        public abstract TapContextReport build();
    }
}
