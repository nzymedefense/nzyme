package app.nzyme.core.rest.responses.ethernet.tls;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class TLSCertificateFullDetailsResponse {

    @JsonProperty("certificate")
    public abstract TLSCertificateDetailsResponse certificate();

    @Nullable
    @JsonProperty("parse_error")
    public abstract String parseError();

    @Nullable
    @JsonProperty("version")
    public abstract Integer version();

    @Nullable
    @JsonProperty("serial")
    public abstract String serial();

    @Nullable
    @JsonProperty("subject_key_identifier")
    public abstract String subjectKeyIdentifier();

    @Nullable
    @JsonProperty("authority_key_identifier")
    public abstract String authorityKeyIdentifier();

    @JsonProperty("pem")
    public abstract String pem();

    @JsonProperty("der_length")
    public abstract int derLength();

    @Nullable
    @JsonProperty("issuer_certificate")
    public abstract TLSChainCertificateResponse issuerCertificate();

    public static Builder builder() {
        return new AutoValue_TLSCertificateFullDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder certificate(TLSCertificateDetailsResponse certificate);
        public abstract Builder parseError(String parseError);
        public abstract Builder version(Integer version);
        public abstract Builder serial(String serial);
        public abstract Builder subjectKeyIdentifier(String subjectKeyIdentifier);
        public abstract Builder authorityKeyIdentifier(String authorityKeyIdentifier);
        public abstract Builder pem(String pem);
        public abstract Builder derLength(int derLength);
        public abstract Builder issuerCertificate(TLSChainCertificateResponse issuerCertificate);
        public abstract TLSCertificateFullDetailsResponse build();
    }
}
