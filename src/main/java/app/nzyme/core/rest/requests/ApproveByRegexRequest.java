package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class ApproveByRegexRequest {

    @Nullable
    public abstract String regex();

    @JsonCreator
    public static ApproveByRegexRequest create(@JsonProperty("regex") String regex) {
        return builder()
                .regex(regex)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ApproveByRegexRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder regex(String regex);

        public abstract ApproveByRegexRequest build();
    }
}
