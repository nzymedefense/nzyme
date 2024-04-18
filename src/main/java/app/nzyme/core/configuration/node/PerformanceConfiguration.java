package app.nzyme.core.configuration.node;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PerformanceConfiguration {

    public abstract int reportProcessorPoolSize();

    public static PerformanceConfiguration create(int reportProcessorPoolSize) {
        return builder()
                .reportProcessorPoolSize(reportProcessorPoolSize)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PerformanceConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder reportProcessorPoolSize(int reportProcessorPoolSize);

        public abstract PerformanceConfiguration build();
    }
}
