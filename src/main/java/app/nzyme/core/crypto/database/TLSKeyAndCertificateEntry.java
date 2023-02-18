package app.nzyme.core.crypto.database;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class TLSKeyAndCertificateEntry {

    public abstract UUID nodeId();
    public abstract String certificate();
    public abstract String key();
    public abstract DateTime validFrom();
    public abstract DateTime expiresAt();

    public static TLSKeyAndCertificateEntry create(UUID nodeId, String certificate, String key, DateTime validFrom, DateTime expiresAt) {
        return builder()
                .nodeId(nodeId)
                .certificate(certificate)
                .key(key)
                .validFrom(validFrom)
                .expiresAt(expiresAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSKeyAndCertificateEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nodeId(UUID nodeId);

        public abstract Builder certificate(String certificate);

        public abstract Builder key(String key);

        public abstract Builder validFrom(DateTime validFrom);

        public abstract Builder expiresAt(DateTime expiresAt);

        public abstract TLSKeyAndCertificateEntry build();
    }

}
