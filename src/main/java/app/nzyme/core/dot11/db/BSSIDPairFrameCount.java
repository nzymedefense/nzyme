package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class BSSIDPairFrameCount {

    public abstract String sender();
    public abstract String receiver();
    public abstract long frameCount();

    public static BSSIDPairFrameCount create(String sender, String receiver, long frameCount) {
        return builder()
                .sender(sender)
                .receiver(receiver)
                .frameCount(frameCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDPairFrameCount.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sender(String sender);

        public abstract Builder receiver(String receiver);

        public abstract Builder frameCount(long frameCount);

        public abstract BSSIDPairFrameCount build();
    }

}
