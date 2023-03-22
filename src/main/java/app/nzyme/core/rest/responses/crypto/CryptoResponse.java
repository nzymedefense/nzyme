package app.nzyme.core.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;


@AutoValue
public abstract class CryptoResponse {

    @JsonProperty("metrics")
    public abstract CryptoNodeMetricsResponse metrics();

    @JsonProperty("pgp_keys")
    public abstract Map<String, PGPKeyResponse> pgpKeys();

    @JsonProperty("tls_certificates")
    public abstract Map<String, TLSCertificateResponse> tlsCertificates();

    @JsonProperty("tls_wildcard_certificates")
    public abstract List<TLSWildcartCertificateResponse> tlsWildcardCertificates();

    @JsonProperty("pgp_keys_in_sync")
    public abstract boolean pgpKeysInSync();

    @JsonProperty("pgp_configuration")
    public abstract PGPConfigurationResponse pgpConfiguration();

    public static CryptoResponse create(CryptoNodeMetricsResponse metrics, Map<String, PGPKeyResponse> pgpKeys, Map<String, TLSCertificateResponse> tlsCertificates, List<TLSWildcartCertificateResponse> tlsWildcardCertificates, boolean pgpKeysInSync, PGPConfigurationResponse pgpConfiguration) {
        return builder()
                .metrics(metrics)
                .pgpKeys(pgpKeys)
                .tlsCertificates(tlsCertificates)
                .tlsWildcardCertificates(tlsWildcardCertificates)
                .pgpKeysInSync(pgpKeysInSync)
                .pgpConfiguration(pgpConfiguration)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CryptoResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder metrics(CryptoNodeMetricsResponse metrics);

        public abstract Builder pgpKeys(Map<String, PGPKeyResponse> pgpKeys);

        public abstract Builder tlsCertificates(Map<String, TLSCertificateResponse> tlsCertificates);

        public abstract Builder tlsWildcardCertificates(List<TLSWildcartCertificateResponse> tlsWildcardCertificates);

        public abstract Builder pgpKeysInSync(boolean pgpKeysInSync);

        public abstract Builder pgpConfiguration(PGPConfigurationResponse pgpConfiguration);

        public abstract CryptoResponse build();
    }
}
