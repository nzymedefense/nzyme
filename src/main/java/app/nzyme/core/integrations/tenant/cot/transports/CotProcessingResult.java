package app.nzyme.core.integrations.tenant.cot.transports;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CotProcessingResult {

    public abstract int bytesSent();
    public abstract int messagesSent();

    public static CotProcessingResult create(int bytesSent, int messagesSent) {
        return builder()
                .bytesSent(bytesSent)
                .messagesSent(messagesSent)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CotProcessingResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bytesSent(int bytesSent);

        public abstract Builder messagesSent(int messagesSent);

        public abstract CotProcessingResult build();
    }
}
