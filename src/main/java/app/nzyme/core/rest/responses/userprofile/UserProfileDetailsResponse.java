package app.nzyme.core.rest.responses.userprofile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UserProfileDetailsResponse {

    @JsonProperty("email")
    public abstract String email();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("unit_system")
    public abstract String unitSystem();

    public static UserProfileDetailsResponse create(String email, String name, String unitSystem) {
        return builder()
                .email(email)
                .name(name)
                .unitSystem(unitSystem)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UserProfileDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder email(String email);

        public abstract Builder name(String name);

        public abstract Builder unitSystem(String unitSystem);

        public abstract UserProfileDetailsResponse build();
    }

}
