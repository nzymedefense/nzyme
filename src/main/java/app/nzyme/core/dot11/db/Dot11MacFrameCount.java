package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Dot11MacFrameCount {

    public abstract String mac();
    public abstract long frameCount();

    public static Dot11MacFrameCount create(String mac, long frameCount) {
        return builder()
                .mac(mac)
                .frameCount(frameCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11MacFrameCount.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(String mac);

        public abstract Builder frameCount(long frameCount);

        public abstract Dot11MacFrameCount build();
    }
}
