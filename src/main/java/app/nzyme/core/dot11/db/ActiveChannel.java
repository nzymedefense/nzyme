package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ActiveChannel {

    public abstract int frequency();
    public abstract long frames();
    public abstract long bytes();

    public static ActiveChannel create(int frequency, long frames, long bytes) {
        return builder()
                .frequency(frequency)
                .frames(frames)
                .bytes(bytes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ActiveChannel.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder frequency(int frequency);

        public abstract Builder frames(long frames);

        public abstract Builder bytes(long bytes);

        public abstract ActiveChannel build();
    }
}
