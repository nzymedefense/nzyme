package app.nzyme.core.events.actions.email;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class EmailActionConfiguration {

    @JsonProperty("subject_prefix")
    public abstract String subjectPrefix();
    @JsonProperty("receivers")
    public abstract List<String> receivers();

    @JsonCreator
    public static EmailActionConfiguration create(@JsonProperty("subject_prefix") String subjectPrefix,
                                                  @JsonProperty("receivers") List<String> receivers) {
        return builder()
                .subjectPrefix(subjectPrefix)
                .receivers(receivers)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EmailActionConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder subjectPrefix(String subjectPrefix);

        public abstract Builder receivers(List<String> receivers);

        public abstract EmailActionConfiguration build();
    }

}
