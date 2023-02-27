package app.nzyme.core.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class TLSCertificateTestResponse {

    @JsonProperty("cert_success")
    public abstract boolean certSuccess();

    @JsonProperty("key_success")
    public abstract boolean keySuccess();

    @JsonProperty("certificate")
    @Nullable
    public abstract TLSCertificateResponse certificate();

    public static TLSCertificateTestResponse create(boolean certSuccess, boolean keySuccess, TLSCertificateResponse certificate) {
        return builder()
                .certSuccess(certSuccess)
                .keySuccess(keySuccess)
                .certificate(certificate)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSCertificateTestResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder certSuccess(boolean certSuccess);

        public abstract Builder keySuccess(boolean keySuccess);

        public abstract Builder certificate(TLSCertificateResponse certificate);

        public abstract TLSCertificateTestResponse build();
    }

}
