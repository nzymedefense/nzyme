package app.nzyme.core.connect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ConnectHealthIndicatorReport {

    @JsonProperty("name")
    public abstract String indicatorName();

    @JsonProperty("id")
    public abstract String indicatorId();

    @JsonProperty("last_checked")
    public abstract DateTime lastChecked();

    @JsonProperty("result_level")
    public abstract String resultLevel();

    @JsonProperty("active")
    public abstract boolean active();

    public static ConnectHealthIndicatorReport create(String indicatorName, String indicatorId, DateTime lastChecked, String resultLevel, boolean active) {
        return builder()
                .indicatorName(indicatorName)
                .indicatorId(indicatorId)
                .lastChecked(lastChecked)
                .resultLevel(resultLevel)
                .active(active)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectHealthIndicatorReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder indicatorName(String indicatorName);

        public abstract Builder indicatorId(String indicatorId);

        public abstract Builder lastChecked(DateTime lastChecked);

        public abstract Builder resultLevel(String resultLevel);

        public abstract Builder active(boolean active);

        public abstract ConnectHealthIndicatorReport build();
    }
}
