package app.nzyme.core.rest.responses.dot11.monitoring.configimport;

import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class BSSIDImportDataResponse {

    @JsonProperty("bssid")
    public abstract Dot11MacAddressResponse bssid();

    @JsonProperty("fingerprints")
    public abstract List<FingerprintImportDataResponse> fingerprints();

    @JsonProperty("exists")
    public abstract boolean exists();

    public static BSSIDImportDataResponse create(Dot11MacAddressResponse bssid, List<FingerprintImportDataResponse> fingerprints, boolean exists) {
        return builder()
                .bssid(bssid)
                .fingerprints(fingerprints)
                .exists(exists)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDImportDataResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(Dot11MacAddressResponse bssid);

        public abstract Builder fingerprints(List<FingerprintImportDataResponse> fingerprints);

        public abstract Builder exists(boolean exists);

        public abstract BSSIDImportDataResponse build();
    }
}
