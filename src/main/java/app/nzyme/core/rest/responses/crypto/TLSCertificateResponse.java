package app.nzyme.core.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class TLSCertificateResponse {

    @JsonProperty("node_id")
    public abstract String nodeId();

    @JsonProperty("node_name")
    public abstract String nodeName();

    @JsonProperty("fingerprint")
    public abstract String fingerprint();

    @JsonProperty("expiration_date")
    public abstract DateTime expirationDate();

    public static TLSCertificateResponse create(String nodeId, String nodeName, String fingerprint, DateTime expirationDate) {
        return builder()
                .nodeId(nodeId)
                .nodeName(nodeName)
                .fingerprint(fingerprint)
                .expirationDate(expirationDate)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSCertificateResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nodeId(String nodeId);

        public abstract Builder nodeName(String nodeName);

        public abstract Builder fingerprint(String fingerprint);

        public abstract Builder expirationDate(DateTime expirationDate);

        public abstract TLSCertificateResponse build();
    }

}
