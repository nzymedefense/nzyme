package app.nzyme.core.environment.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EnvironmentCondition {

    public abstract String displayName();
    public abstract int severity();

    @JsonCreator
    public static EnvironmentCondition create(@JsonProperty("display_name") String displayName,
                                              @JsonProperty("severity")int severity) {
        return builder()
                .displayName(displayName)
                .severity(severity)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EnvironmentCondition.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder displayName(String displayName);

        public abstract Builder severity(int severity);

        public abstract EnvironmentCondition build();
    }
}
