package horse.wtf.nzyme.rest.responses.taps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TapSecretResponse {

    @JsonProperty("secret")
    public abstract String secret();

    public static TapSecretResponse create(String secret) {
        return builder()
                .secret(secret)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapSecretResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder secret(String secret);

        public abstract TapSecretResponse build();
    }

}
