package app.nzyme.core.rest.responses.misc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ErrorResponse {

    @JsonProperty("message")
    public abstract String message();

    public static ErrorResponse create(String message) {
        return builder()
                .message(message)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ErrorResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder message(String message);

        public abstract ErrorResponse build();
    }

}
