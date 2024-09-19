package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class UpdateConfigurationRequest {

    public abstract Map<String, Object> change();

    @JsonCreator
    public static UpdateConfigurationRequest create(@JsonProperty("change") Map<String, Object> change) {
        return builder()
                .change(change)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateConfigurationRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder change(Map<String, Object> change);

        public abstract UpdateConfigurationRequest build();
    }
}
