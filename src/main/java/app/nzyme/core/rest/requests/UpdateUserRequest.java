package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UpdateUserRequest {

    public abstract String email();
    public abstract String name();

    @JsonCreator
    public static UpdateUserRequest create(@JsonProperty("email") String email, @JsonProperty("name") String name) {
        return builder()
                .email(email)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateUserRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder email(String email);

        public abstract Builder name(String name);

        public abstract UpdateUserRequest build();
    }

}
