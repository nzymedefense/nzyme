package app.nzyme.core.rest.responses.nodes;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class NodesConfigurationResponse {

    @JsonProperty("ephemeral_nodes_regex")
    public abstract ConfigurationEntryResponse ephemeralNodesRegex();

    public static NodesConfigurationResponse create(ConfigurationEntryResponse ephemeralNodesRegex) {
        return builder()
                .ephemeralNodesRegex(ephemeralNodesRegex)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NodesConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ephemeralNodesRegex(ConfigurationEntryResponse ephemeralNodesRegex);

        public abstract NodesConfigurationResponse build();
    }

}
