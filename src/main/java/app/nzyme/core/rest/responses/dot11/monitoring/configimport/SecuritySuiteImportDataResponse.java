package app.nzyme.core.rest.responses.dot11.monitoring.configimport;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SecuritySuiteImportDataResponse {

    @JsonProperty("security_suite")
    public abstract String securitySuite();

    @JsonProperty("exists")
    public abstract boolean exists();

    public static SecuritySuiteImportDataResponse create(String securitySuite, boolean exists) {
        return builder()
                .securitySuite(securitySuite)
                .exists(exists)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SecuritySuiteImportDataResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder securitySuite(String securitySuite);

        public abstract Builder exists(boolean exists);

        public abstract SecuritySuiteImportDataResponse build();
    }
}
