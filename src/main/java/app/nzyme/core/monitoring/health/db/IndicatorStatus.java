package app.nzyme.core.monitoring.health.db;

import app.nzyme.core.monitoring.health.Indicator;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class IndicatorStatus {

    public abstract String indicatorName();
    public abstract String indicatorId();
    public abstract DateTime lastChecked();
    public abstract String resultLevel();

    public static IndicatorStatus red(Indicator indicator) {
        return create(indicator.getName(), indicator.getId(), DateTime.now(), "RED");
    }

    public static IndicatorStatus orange(Indicator indicator) {
        return create(indicator.getName(), indicator.getId(), DateTime.now(), "ORANGE");
    }

    public static IndicatorStatus green(Indicator indicator) {
        return create(indicator.getName(), indicator.getId(), DateTime.now(), "GREEN");
    }

    public static IndicatorStatus create(String indicatorName, String indicatorId, DateTime lastChecked, String resultLevel) {
        return builder()
                .indicatorName(indicatorName)
                .indicatorId(indicatorId)
                .lastChecked(lastChecked)
                .resultLevel(resultLevel)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_IndicatorStatus.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder indicatorName(String indicatorName);

        public abstract Builder indicatorId(String indicatorId);

        public abstract Builder lastChecked(DateTime lastChecked);

        public abstract Builder resultLevel(String resultLevel);

        public abstract IndicatorStatus build();
    }

}
