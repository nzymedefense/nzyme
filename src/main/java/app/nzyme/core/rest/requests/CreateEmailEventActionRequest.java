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
        public abstract Builder name(@NotEmpty String name);

        public abstract Builder description(@NotEmpty String description);

        public abstract Builder subjectPrefix(@NotEmpty String subjectPrefix);

        public abstract Builder receivers(@NotNull List<String> receivers);

        public abstract Builder organizationId(@Nullable UUID organizationId);

        public abstract CreateEmailEventActionRequest build();
    }
}
