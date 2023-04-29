package app.nzyme.core.rest.responses.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class MFAInitResponse {

    @JsonProperty("user_secret")
    public abstract String userSecret();

    @JsonProperty("user_email")
    public abstract String userEmail();

    @JsonProperty("recovery_codes")
    public abstract List<String> recoveryCodes();

    public static MFAInitResponse create(String userSecret, String userEmail, List<String> recoveryCodes) {
        return builder()
                .userSecret(userSecret)
                .userEmail(userEmail)
                .recoveryCodes(recoveryCodes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MFAInitResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder userSecret(String userSecret);

        public abstract Builder userEmail(String userEmail);

        public abstract Builder recoveryCodes(List<String> recoveryCodes);

        public abstract MFAInitResponse build();
    }
}
