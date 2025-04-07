package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@AutoValue
public abstract class CreateUserRequest {

    @NotEmpty
    public abstract String email();

    @NotEmpty
    public abstract String password();

    @NotEmpty
    public abstract String name();

    @NotNull
    public abstract Boolean disableMfa();

    @JsonCreator
    public static CreateUserRequest create(@JsonProperty("email") String email,
                                           @JsonProperty("password") String password,
                                           @JsonProperty("name") String name,
                                           @JsonProperty("disable_mfa") Boolean disableMfa) {
        return builder()
                .email(email)
                .password(password)
                .name(name)
                .disableMfa(disableMfa)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateUserRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder email(String email);

        public abstract Builder password(String password);

        public abstract Builder name(String name);

        public abstract Builder disableMfa(Boolean disableMfa);

        public abstract CreateUserRequest build();
    }

}
