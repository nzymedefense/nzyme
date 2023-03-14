package app.nzyme.core.crypto.tls;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class KeyStoreBootstrapResult {

    public abstract byte[] keystoreBytes();
    public abstract TLSKeyAndCertificate loadedCertificate();

    public static KeyStoreBootstrapResult create(byte[] keystoreBytes, TLSKeyAndCertificate loadedCertificate) {
        return builder()
                .keystoreBytes(keystoreBytes)
                .loadedCertificate(loadedCertificate)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_KeyStoreBootstrapResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder keystoreBytes(byte[] keystoreBytes);

        public abstract Builder loadedCertificate(TLSKeyAndCertificate loadedCertificate);

        public abstract KeyStoreBootstrapResult build();
    }

}
