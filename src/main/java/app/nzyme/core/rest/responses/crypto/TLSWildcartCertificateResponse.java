package app.nzyme.core.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class TLSWildcartCertificateResponse {

    @JsonProperty("id")
    public abstract long id();

    @JsonProperty("node_matcher")
    public abstract String nodeMatcher();

    @JsonProperty("sourcetype")
    public abstract String sourceType();

    @JsonProperty("fingerprint")
    public abstract String fingerprint();

    @JsonProperty("signature_algorithm")
    public abstract String signatureAlgorithm();

    @JsonProperty("issuer")
    @Nullable
    public abstract TLSCertificatePrincipalResponse issuer();

    @JsonProperty("subject")
    @Nullable
    public abstract TLSCertificatePrincipalResponse subject();

    @JsonProperty("valid_from")
    public abstract DateTime validFrom();

    @JsonProperty("expires_at")
    public abstract DateTime expiresAt();

    public static TLSWildcartCertificateResponse create(long id, String nodeMatcher, String sourceType, String fingerprint, String signatureAlgorithm, TLSCertificatePrincipalResponse issuer, TLSCertificatePrincipalResponse subject, DateTime validFrom, DateTime expiresAt) {
        return builder()
                .id(id)
                .nodeMatcher(nodeMatcher)
                .sourceType(sourceType)
                .fingerprint(fingerprint)
                .signatureAlgorithm(signatureAlgorithm)
                .issuer(issuer)
                .subject(subject)
                .validFrom(validFrom)
                .expiresAt(expiresAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSWildcartCertificateResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder nodeMatcher(String nodeMatcher);

        public abstract Builder sourceType(String sourceType);

        public abstract Builder fingerprint(String fingerprint);

        public abstract Builder signatureAlgorithm(String signatureAlgorithm);

        public abstract Builder issuer(TLSCertificatePrincipalResponse issuer);

        public abstract Builder subject(TLSCertificatePrincipalResponse subject);

        public abstract Builder validFrom(DateTime validFrom);

        public abstract Builder expiresAt(DateTime expiresAt);

        public abstract TLSWildcartCertificateResponse build();
    }

}
