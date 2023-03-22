package app.nzyme.core.crypto.pgp;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PGPKeys {

    public abstract byte[] privateKey();
    public abstract byte[] publicKey();

    public static PGPKeys create(byte[] privateKey, byte[] publicKey) {
        return builder()
                .privateKey(privateKey)
                .publicKey(publicKey)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PGPKeys.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder privateKey(byte[] privateKey);

        public abstract Builder publicKey(byte[] publicKey);

        public abstract PGPKeys build();
    }

}
