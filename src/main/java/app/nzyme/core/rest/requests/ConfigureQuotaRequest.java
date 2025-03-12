package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.Min;

@AutoValue
public abstract class ConfigureQuotaRequest {

    @Min(0)
    public abstract int quota();

    @JsonCreator
    public static ConfigureQuotaRequest create(@JsonProperty("quota") int quota) {
        return builder()
                .quota(quota)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConfigureQuotaRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder quota(int quota);

        public abstract ConfigureQuotaRequest build();
    }
}
