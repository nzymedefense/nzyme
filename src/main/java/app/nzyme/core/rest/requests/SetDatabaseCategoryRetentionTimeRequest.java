package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.Min;

@AutoValue
public abstract class SetDatabaseCategoryRetentionTimeRequest {

    @Min(1)
    public abstract int retentionTimeDays();

    @JsonCreator
    public static SetDatabaseCategoryRetentionTimeRequest create(@JsonProperty("retention_time_days") int retentionTimeDays) {
        return builder()
                .retentionTimeDays(retentionTimeDays)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SetDatabaseCategoryRetentionTimeRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder retentionTimeDays(@Min(1) int retentionTimeDays);

        public abstract SetDatabaseCategoryRetentionTimeRequest build();
    }
}
