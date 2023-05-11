package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UpdateUserOwnPasswordRequest {

    public abstract String currentPassword();
    public abstract String newPassword();

    @JsonCreator
    public static UpdateUserOwnPasswordRequest create(@JsonProperty("current_password") String currentPassword,
                                                      @JsonProperty("new_password") String newPassword) {
        return builder()
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateUserOwnPasswordRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder currentPassword(String currentPassword);

        public abstract Builder newPassword(String newPassword);

        public abstract UpdateUserOwnPasswordRequest build();
    }

}
