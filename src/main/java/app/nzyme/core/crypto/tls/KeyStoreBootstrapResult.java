package app.nzyme.core.crypto.tls;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class KeyStoreBootstrapResult {

    public abstract byte[] keystoreBytes();
    public abstract TLSSourceType loadSource();

    public static KeyStoreBootstrapResult create(byte[] keystoreBytes, TLSSourceType loadSource) {
        return builder()
                .keystoreBytes(keystoreBytes)
                .loadSource(loadSource)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_KeyStoreBootstrapResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder keystoreBytes(byte[] keystoreBytes);

        public abstract Builder loadSource(TLSSourceType loadSource);

        public abstract KeyStoreBootstrapResult build();
    }

}
