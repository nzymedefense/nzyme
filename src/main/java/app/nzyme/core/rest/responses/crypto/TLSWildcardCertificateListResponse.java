package app.nzyme.core.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TLSWildcardCertificateListResponse {

    @JsonProperty("certificates")
    public abstract List<TLSWildcartCertificateResponse> certificates();

    public static TLSWildcardCertificateListResponse create(List<TLSWildcartCertificateResponse> certificates) {
        return builder()
                .certificates(certificates)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSWildcardCertificateListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder certificates(List<TLSWildcartCertificateResponse> certificates);

        public abstract TLSWildcardCertificateListResponse build();
    }

}
