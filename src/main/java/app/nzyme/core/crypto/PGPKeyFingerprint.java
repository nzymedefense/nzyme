package app.nzyme.core.crypto;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class PGPKeyFingerprint {

    public abstract UUID nodeId();
    public abstract String nodeName();
    public abstract String fingerprint();
    public abstract DateTime createdAt();

    public static PGPKeyFingerprint create(UUID nodeId, String nodeName, String fingerprint, DateTime createdAt) {
        return builder()
                .nodeId(nodeId)
                .nodeName(nodeName)
                .fingerprint(fingerprint)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PGPKeyFingerprint.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nodeId(UUID nodeId);

        public abstract Builder nodeName(String nodeName);

        public abstract Builder fingerprint(String fingerprint);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract PGPKeyFingerprint build();
    }
}
