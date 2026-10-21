package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import io.swagger.v3.oas.annotations.media.Schema;

@AutoValue
public abstract class BeaconBulkClassificationResponse {

    @JsonProperty("updated_count")
    @Schema(description = "Number of beacons whose classification was set.")
    public abstract long updatedCount();

    public static BeaconBulkClassificationResponse create(long updatedCount) {
        return builder()
                .updatedCount(updatedCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconBulkClassificationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder updatedCount(long updatedCount);
        public abstract BeaconBulkClassificationResponse build();
    }

}
