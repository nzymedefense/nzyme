package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UpdatePasswordRequest {

    public abstract String password();

    @JsonCreator
    public static UpdatePasswordRequest create(@JsonProperty("password") String password) {
        return builder()
                .password(password)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdatePasswordRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder password(String password);

        public abstract UpdatePasswordRequest build();
    }

}
