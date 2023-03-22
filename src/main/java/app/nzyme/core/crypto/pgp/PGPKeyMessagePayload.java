package app.nzyme.core.crypto.pgp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PGPKeyMessagePayload {

    @JsonProperty("public_key")
    public abstract String publicKey();

    @JsonProperty("private_key")
    public abstract String privateKey();

    @JsonCreator
    public static PGPKeyMessagePayload create(@JsonProperty("public_key") String publicKey, @JsonProperty("private_key") String privateKey) {
        return builder()
                .publicKey(publicKey)
                .privateKey(privateKey)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PGPKeyMessagePayload.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder publicKey(String publicKey);

        public abstract Builder privateKey(String privateKey);

        public abstract PGPKeyMessagePayload build();
    }

}
