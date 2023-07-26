package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotEmpty;

@AutoValue
public abstract class CreateDot11MonitoredSecuritySuiteRequest {

    @NotEmpty
    public abstract String suite();

    @JsonCreator
    public static CreateDot11MonitoredSecuritySuiteRequest create(@JsonProperty("suite") String suite) {
        return builder()
                .suite(suite)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateDot11MonitoredSecuritySuiteRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder suite(String suite);

        public abstract CreateDot11MonitoredSecuritySuiteRequest build();
    }

}
