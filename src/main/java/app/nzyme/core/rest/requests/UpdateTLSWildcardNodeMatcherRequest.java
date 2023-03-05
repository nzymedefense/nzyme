package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UpdateTLSWildcardNodeMatcherRequest {

    public abstract String nodeMatcher();

    @JsonCreator
    public static UpdateTLSWildcardNodeMatcherRequest create(@JsonProperty("node_matcher") String nodeMatcher) {
        return builder()
                .nodeMatcher(nodeMatcher)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateTLSWildcardNodeMatcherRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nodeMatcher(String nodeMatcher);

        public abstract UpdateTLSWildcardNodeMatcherRequest build();
    }

}
