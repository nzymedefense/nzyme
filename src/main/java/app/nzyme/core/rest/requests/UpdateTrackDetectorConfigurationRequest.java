package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@AutoValue
public abstract class UpdateTrackDetectorConfigurationRequest {

    public abstract UUID tapId();
    public abstract long frameThreshold();
    public abstract long gapThreshold();
    public abstract long signalCenterlineJitter();

    @JsonCreator
    public static UpdateTrackDetectorConfigurationRequest create(@JsonProperty("tap_id") @NotNull UUID tapId,
                                                                 @JsonProperty("frame_threshold") long frameThreshold,
                                                                 @JsonProperty("gap_threshold")  long gapThreshold,
                                                                 @JsonProperty("signal_centerline_jitter") long signalCenterlineJitter) {
        return builder()
                .tapId(tapId)
                .frameThreshold(frameThreshold)
                .gapThreshold(gapThreshold)
                .signalCenterlineJitter(signalCenterlineJitter)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateTrackDetectorConfigurationRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tapId(UUID tapId);

        public abstract Builder frameThreshold(long frameThreshold);

        public abstract Builder gapThreshold(long gapThreshold);

        public abstract Builder signalCenterlineJitter(long signalCenterlineJitter);

        public abstract UpdateTrackDetectorConfigurationRequest build();
    }

}
