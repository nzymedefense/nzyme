package app.nzyme.core.rest.responses.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class SessionUserInformationDetailsResponse {

    @JsonProperty("id")
    public abstract UUID id();

    @JsonProperty("email")
    public abstract String email();

    @JsonProperty("name")
    public abstract String name();

    public static SessionUserInformationDetailsResponse create(UUID id, String email, String name) {
        return builder()
                .id(id)
                .email(email)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SessionUserInformationDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder email(String email);

        public abstract Builder name(String name);

        public abstract SessionUserInformationDetailsResponse build();
    }
}
