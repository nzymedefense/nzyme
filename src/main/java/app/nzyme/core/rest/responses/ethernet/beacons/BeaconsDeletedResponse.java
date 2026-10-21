package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import io.swagger.v3.oas.annotations.media.Schema;

@AutoValue
public abstract class BeaconsDeletedResponse {

    @JsonProperty("deleted_count")
    @Schema(description = "Number of beacons that were deleted.")
    public abstract long deletedCount();

    public static BeaconsDeletedResponse create(long deletedCount) {
        return builder()
                .deletedCount(deletedCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconsDeletedResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder deletedCount(long deletedCount);
        public abstract BeaconsDeletedResponse build();
    }

}
