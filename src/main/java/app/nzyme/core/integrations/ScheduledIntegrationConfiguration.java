package app.nzyme.core.integrations;

import com.google.auto.value.AutoValue;

import java.util.concurrent.TimeUnit;

@AutoValue
public abstract class ScheduledIntegrationConfiguration {

    public abstract long initialDelay();
    public abstract long period();
    public abstract TimeUnit timeUnit();

    public static ScheduledIntegrationConfiguration create(long initialDelay, long period, TimeUnit timeUnit) {
        return builder()
                .initialDelay(initialDelay)
                .period(period)
                .timeUnit(timeUnit)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ScheduledIntegrationConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder initialDelay(long initialDelay);

        public abstract Builder period(long period);

        public abstract Builder timeUnit(TimeUnit timeUnit);

        public abstract ScheduledIntegrationConfiguration build();
    }

}
