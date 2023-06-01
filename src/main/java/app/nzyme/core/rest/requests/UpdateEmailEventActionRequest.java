package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class UpdateEmailEventActionRequest {

    @NotEmpty
    public abstract String name();

    @NotEmpty
    public abstract String description();

    @NotEmpty
    public abstract String subjectPrefix();

    @NotNull
    public abstract List<String> receivers();

    @JsonCreator
    public static UpdateEmailEventActionRequest create(@JsonProperty("name") String name,
                                                       @JsonProperty("description") String description,
                                                       @JsonProperty("subject_prefix") String subjectPrefix,
                                                       @JsonProperty("receivers") List<String> receivers) {
        return builder()
                .name(name)
                .description(description)
                .subjectPrefix(subjectPrefix)
                .receivers(receivers)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateEmailEventActionRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder subjectPrefix(String subjectPrefix);

        public abstract Builder receivers(List<String> receivers);

        public abstract UpdateEmailEventActionRequest build();
    }
}
