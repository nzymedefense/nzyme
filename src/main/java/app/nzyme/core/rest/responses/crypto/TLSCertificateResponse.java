package app.nzyme.core.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class TLSCertificateResponse {

    @JsonProperty("node_id")
    public abstract String nodeId();

    @JsonProperty("sourcetype")
    public abstract String sourceType();

    @JsonProperty("node_name")
    public abstract String nodeName();

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

    public static TLSCertificateResponse create(String nodeId, String sourceType, String nodeName, String fingerprint, String signatureAlgorithm, TLSCertificatePrincipalResponse issuer, TLSCertificatePrincipalResponse subject, DateTime validFrom, DateTime expiresAt) {
        return builder()
                .nodeId(nodeId)
                .sourceType(sourceType)
                .nodeName(nodeName)
                .fingerprint(fingerprint)
                .signatureAlgorithm(signatureAlgorithm)
                .issuer(issuer)
                .subject(subject)
                .validFrom(validFrom)
                .expiresAt(expiresAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSCertificateResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nodeId(String nodeId);

        public abstract Builder sourceType(String sourceType);

        public abstract Builder nodeName(String nodeName);

        public abstract Builder fingerprint(String fingerprint);

        public abstract Builder signatureAlgorithm(String signatureAlgorithm);

        public abstract Builder issuer(TLSCertificatePrincipalResponse issuer);

        public abstract Builder subject(TLSCertificatePrincipalResponse subject);

        public abstract Builder validFrom(DateTime validFrom);

        public abstract Builder expiresAt(DateTime expiresAt);

        public abstract TLSCertificateResponse build();
    }

}
