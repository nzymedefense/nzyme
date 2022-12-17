package horse.wtf.nzyme.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;


@AutoValue
public abstract class CryptoResponse {

    @JsonProperty("metrics")
    public abstract CryptoMetricsResponse metrics();

    @JsonProperty("pgp_keys")
    public abstract Map<String, PGPKeyResponse> pgpKeys();

    public static CryptoResponse create(CryptoMetricsResponse metrics, Map<String, PGPKeyResponse> pgpKeys) {
        return builder()
                .metrics(metrics)
                .pgpKeys(pgpKeys)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CryptoResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder metrics(CryptoMetricsResponse metrics);

        public abstract Builder pgpKeys(Map<String, PGPKeyResponse> pgpKeys);

        public abstract CryptoResponse build();
    }

}
