package app.nzyme.core.rest.requests;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class MFAVerificationRequest {

    public abstract String code();

    public static MFAVerificationRequest create(String code) {
        return builder()
                .code(code)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MFAVerificationRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder code(String code);

        public abstract MFAVerificationRequest build();
    }
}
