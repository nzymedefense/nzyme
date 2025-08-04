package app.nzyme.core.rest.resources.taps.reports.tables.gnss;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class GNSSConstellationsReport{

    public abstract Map<String, GNSSConstellationReport> constellations();

    @JsonCreator
    public static GNSSConstellationsReport create(@JsonProperty("constellations") Map<String, GNSSConstellationReport> constellations) {
        return builder()
                .constellations(constellations)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSConstellationsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder constellations(Map<String, GNSSConstellationReport> constellations);

        public abstract GNSSConstellationsReport build();
    }
}
