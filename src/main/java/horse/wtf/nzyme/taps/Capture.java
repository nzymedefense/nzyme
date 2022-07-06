package horse.wtf.nzyme.taps;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class Capture {

    public abstract String interfaceName();
    public abstract String captureType();
    public abstract Boolean isRunning();
    public abstract Long received();
    public abstract Long droppedBuffer();
    public abstract Long droppedInterface();
    public abstract DateTime updatedAt();
    public abstract DateTime createdAt();

    public static Capture create(String interfaceName, String captureType, Boolean isRunning, Long received, Long droppedBuffer, Long droppedInterface, DateTime updatedAt, DateTime createdAt) {
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
        return new AutoValue_Capture.Builder();
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

        public abstract Capture build();
    }

}
