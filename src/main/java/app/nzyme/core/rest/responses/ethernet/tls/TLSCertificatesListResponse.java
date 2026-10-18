package app.nzyme.core.rest.responses.ethernet.tls;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TLSCertificatesListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("certificates")
    public abstract List<TLSCertificateDetailsResponse> certificates();

    public static TLSCertificatesListResponse create(long total, List<TLSCertificateDetailsResponse> certificates) {
        return builder()
                .total(total)
                .certificates(certificates)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSCertificatesListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder certificates(List<TLSCertificateDetailsResponse> certificates);

        public abstract TLSCertificatesListResponse build();
    }
}
