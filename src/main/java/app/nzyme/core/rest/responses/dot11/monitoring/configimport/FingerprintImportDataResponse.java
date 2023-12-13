package app.nzyme.core.rest.responses.dot11.monitoring.configimport;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FingerprintImportDataResponse {

    @JsonProperty("fingerprint")
    public abstract String fingerprint();

    @JsonProperty("exists")
    public abstract boolean exists();

    public static FingerprintImportDataResponse create(String fingerprint, boolean exists) {
        return builder()
                .fingerprint(fingerprint)
                .exists(exists)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_FingerprintImportDataResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder fingerprint(String fingerprint);

        public abstract Builder exists(boolean exists);

        public abstract FingerprintImportDataResponse build();
    }
}
