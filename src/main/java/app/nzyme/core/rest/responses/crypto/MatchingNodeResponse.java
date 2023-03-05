package app.nzyme.core.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class MatchingNodeResponse {

    @JsonProperty("node_id")
    public abstract UUID nodeId();

    @JsonProperty("node_name")
    public abstract String nodeName();

    public static MatchingNodeResponse create(UUID nodeId, String nodeName) {
        return builder()
                .nodeId(nodeId)
                .nodeName(nodeName)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MatchingNodeResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nodeId(UUID nodeId);

        public abstract Builder nodeName(String nodeName);

        public abstract MatchingNodeResponse build();
    }

}
