package app.nzyme.core.rest.responses.ethernet.recon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ReconScansDeletedResponse {

    @JsonProperty("deleted_count")
    public abstract int deletedCount();

    public static ReconScansDeletedResponse create(int deletedCount) {
        return builder()
                .deletedCount(deletedCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconScansDeletedResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder deletedCount(int deletedCount);
        public abstract ReconScansDeletedResponse build();
    }
}
