package horse.wtf.nzyme.crypto;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class PGPKeyFingerprint {

    public abstract String node();
    public abstract String fingerprint();
    public abstract DateTime createdAt();

    public static PGPKeyFingerprint create(String node, String fingerprint, DateTime createdAt) {
        return builder()
                .node(node)
                .fingerprint(fingerprint)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PGPKeyFingerprint.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder node(String node);

        public abstract Builder fingerprint(String fingerprint);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract PGPKeyFingerprint build();
    }

}
