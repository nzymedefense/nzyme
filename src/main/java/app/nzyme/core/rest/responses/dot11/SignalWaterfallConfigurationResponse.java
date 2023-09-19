package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SignalWaterfallConfigurationResponse {

    @JsonProperty("frame_threshold")
    public abstract long frameThreshold();

    @JsonProperty("frame_threshold_default")
    public abstract long frameThresholdDefault();

    @JsonProperty("gap_threshold")
    public abstract long gapThreshold();

    @JsonProperty("gap_threshold_default")
    public abstract long gapThresholdDefault();

    @JsonProperty("signal_centerline_jitter")
    public abstract long signalCenterlineJitter();

    @JsonProperty("signal_centerline_jitter_default")
    public abstract long signalCenterlineJitterDefault();

    public static SignalWaterfallConfigurationResponse create(long frameThreshold, long frameThresholdDefault, long gapThreshold, long gapThresholdDefault, long signalCenterlineJitter, long signalCenterlineJitterDefault) {
        return builder()
                .frameThreshold(frameThreshold)
                .frameThresholdDefault(frameThresholdDefault)
                .gapThreshold(gapThreshold)
                .gapThresholdDefault(gapThresholdDefault)
                .signalCenterlineJitter(signalCenterlineJitter)
                .signalCenterlineJitterDefault(signalCenterlineJitterDefault)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SignalWaterfallConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder frameThreshold(long frameThreshold);

        public abstract Builder frameThresholdDefault(long frameThresholdDefault);

        public abstract Builder gapThreshold(long gapThreshold);

        public abstract Builder gapThresholdDefault(long gapThresholdDefault);

        public abstract Builder signalCenterlineJitter(long signalCenterlineJitter);

        public abstract Builder signalCenterlineJitterDefault(long signalCenterlineJitterDefault);

        public abstract SignalWaterfallConfigurationResponse build();
    }
}
