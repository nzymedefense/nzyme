package app.nzyme.core.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class TLSWildcartCertificateResponse {

    @JsonProperty("id")
    public abstract long id();

    @JsonProperty("node_matcher")
    public abstract String nodeMatcher();

    @JsonProperty("matching_nodes")
    public abstract List<MatchingNodeResponse> matchingNodes();

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

    public static TLSWildcartCertificateResponse create(long id, String nodeMatcher, List<MatchingNodeResponse> matchingNodes, String sourceType, String fingerprint, String signatureAlgorithm, TLSCertificatePrincipalResponse issuer, TLSCertificatePrincipalResponse subject, DateTime validFrom, DateTime expiresAt) {
        return builder()
                .id(id)
                .nodeMatcher(nodeMatcher)
                .matchingNodes(matchingNodes)
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

        public abstract Builder matchingNodes(List<MatchingNodeResponse> matchingNodes);

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
