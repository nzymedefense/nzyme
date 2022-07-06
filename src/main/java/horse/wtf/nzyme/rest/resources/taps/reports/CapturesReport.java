package horse.wtf.nzyme.rest.resources.taps.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CapturesReport {

    public abstract String captureType();
    public abstract String interfaceName();
    public abstract Boolean isRunning();
    public abstract Long received();
    public abstract Long droppedBuffer();
    public abstract Long droppedInterface();

    @JsonCreator
    public static CapturesReport create(@JsonProperty("capture_type") String captureType,
                                        @JsonProperty("interface_name") String interfaceName,
                                        @JsonProperty("is_running") Boolean isRunning,
                                        @JsonProperty("received") Long received,
                                        @JsonProperty("dropped_buffer") Long droppedBuffer,
                                        @JsonProperty("dropped_interface") Long droppedInterface) {
        return builder()
                .captureType(captureType)
                .interfaceName(interfaceName)
                .isRunning(isRunning)
                .received(received)
                .droppedBuffer(droppedBuffer)
                .droppedInterface(droppedInterface)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CapturesReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder captureType(String captureType);

        public abstract Builder interfaceName(String interfaceName);

        public abstract Builder isRunning(Boolean isRunning);

        public abstract Builder received(Long received);

        public abstract Builder droppedBuffer(Long droppedBuffer);

        public abstract Builder droppedInterface(Long droppedInterface);

        public abstract CapturesReport build();
    }
}
