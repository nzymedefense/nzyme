package horse.wtf.nzyme.rest.responses.taps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class CaptureDetailsResponse {

    @JsonProperty("interface_name")
    public abstract String interfaceName();

    @JsonProperty("capture_type")
    public abstract String captureType();

    @JsonProperty("is_running")
    public abstract Boolean isRunning();

    @JsonProperty("received")
    public abstract Long received();

    @JsonProperty("dropped_buffer")
    public abstract Long droppedBuffer();

    @JsonProperty("dropped_interface")
    public abstract Long droppedInterface();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static CaptureDetailsResponse create(String interfaceName, String captureType, Boolean isRunning, Long received, Long droppedBuffer, Long droppedInterface, DateTime updatedAt, DateTime createdAt) {
        return builder()
                .interfaceName(interfaceName)
                .captureType(captureType)
                .isRunning(isRunning)
                .received(received)
                .droppedBuffer(droppedBuffer)
                .droppedInterface(droppedInterface)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CaptureDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder interfaceName(String interfaceName);

        public abstract Builder captureType(String captureType);

        public abstract Builder isRunning(Boolean isRunning);

        public abstract Builder received(Long received);

        public abstract Builder droppedBuffer(Long droppedBuffer);

        public abstract Builder droppedInterface(Long droppedInterface);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract CaptureDetailsResponse build();
    }

}
