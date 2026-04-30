package app.nzyme.core.rest.responses.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class LLMResponse {

    @JsonProperty("input_tokens")
    public abstract int inputTokens();
    @JsonProperty("output_tokens")
    public abstract int outputTokens();
    @JsonProperty("response")
    public abstract Map<String, Object> response();

    public static LLMResponse create(int inputTokens, int outputTokens, Map<String, Object> response) {
        return builder()
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .response(response)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_LLMResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder inputTokens(int inputTokens);

        public abstract Builder outputTokens(int outputTokens);

        public abstract Builder response(Map<String, Object> response);

        public abstract LLMResponse build();
    }
}
