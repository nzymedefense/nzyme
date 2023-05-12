package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class MFARecoveryCodeRequest {

    public abstract String code();

    @JsonCreator
    public static MFARecoveryCodeRequest create(@JsonProperty("code") String code) {
        return builder()
                .code(code)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MFARecoveryCodeRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder code(String code);

        public abstract MFARecoveryCodeRequest build();
    }

}
