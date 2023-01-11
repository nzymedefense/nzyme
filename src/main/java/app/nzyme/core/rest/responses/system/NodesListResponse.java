package app.nzyme.core.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class NodesListResponse {

    @JsonProperty("nodes")
    public abstract List<NodeResponse> nodes();

    public static NodesListResponse create(List<NodeResponse> nodes) {
        return builder()
                .nodes(nodes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NodesListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nodes(List<NodeResponse> nodes);

        public abstract NodesListResponse build();
    }
}
