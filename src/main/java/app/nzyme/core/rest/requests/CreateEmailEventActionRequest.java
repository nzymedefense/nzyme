package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class CreateEmailEventActionRequest {

    @NotEmpty
    public abstract String name();

    @NotEmpty
    public abstract String description();

    @NotEmpty
    public abstract String subjectPrefix();

    @NotNull
    public abstract List<String> receivers();

    @Nullable
    public abstract UUID organizationId();

    @JsonCreator
    public static CreateEmailEventActionRequest create(@JsonProperty("name") String name,
                                                       @JsonProperty("description") String description,
                                                       @JsonProperty("subject_prefix") String subjectPrefix,
                                                       @JsonProperty("receivers") List<String> receivers,
                                                       @JsonProperty("organization_id") UUID organizationId) {
        return builder()
                .name(name)
                .description(description)
                .subjectPrefix(subjectPrefix)
                .receivers(receivers)
                .organizationId(organizationId)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateEmailEventActionRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder subjectPrefix(String subjectPrefix);

        public abstract Builder receivers(List<String> receivers);

        public abstract Builder organizationId(UUID organizationId);

        public abstract CreateEmailEventActionRequest build();
    }
}
