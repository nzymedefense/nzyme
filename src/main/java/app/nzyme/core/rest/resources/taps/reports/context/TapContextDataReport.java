package app.nzyme.core.rest.resources.taps.reports.context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class TapContextDataReport {

    public abstract String value();
    public abstract String source();
    public abstract DateTime lastSeen();

    @JsonCreator
    public static TapContextDataReport create(@JsonProperty("value") String value,
                                              @JsonProperty("source") String source,
                                              @JsonProperty("last_seen") DateTime lastSeen) {
        return builder()
                .value(value)
                .source(source)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapContextDataReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder value(String value);

        public abstract Builder source(String source);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract TapContextDataReport build();
    }
}
