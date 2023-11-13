package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotEmpty;

@AutoValue
public abstract class CreateUserRequest {

    @NotEmpty
    public abstract String email();

    @NotEmpty
    public abstract String password();

    @NotEmpty
    public abstract String name();

    @JsonCreator
    public static CreateUserRequest create(@JsonProperty("email") String email,
                                           @JsonProperty("password") String password,
                                           @JsonProperty("name") String name) {
        return builder()
                .email(email)
                .password(password)
                .name(name)
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

        public abstract CreateUserRequest build();
    }

}
