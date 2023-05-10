package app.nzyme.core.rest.responses.userprofile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UserProfileDetailsResponse {

    @JsonProperty("email")
    public abstract String email();

    @JsonProperty("name")
    public abstract String name();

    public static UserProfileDetailsResponse create(String email, String name) {
        return builder()
                .email(email)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UserProfileDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder email(String email);

        public abstract Builder name(String name);

        public abstract UserProfileDetailsResponse build();
    }
}
