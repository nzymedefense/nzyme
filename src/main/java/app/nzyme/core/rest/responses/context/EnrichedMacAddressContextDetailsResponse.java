package app.nzyme.core.rest.responses.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class EnrichedMacAddressContextDetailsResponse {

    @JsonProperty("context")
    public abstract MacAddressContextDetailsResponse context();

    @JsonProperty("context_type")
    public abstract MacAddressContextTypeResponse contextType();

    @JsonProperty("serves_dot11_monitored_network")
    @Nullable
    public abstract Dot11MonitoredNetworkContextResponse servesDot11MonitoredNetwork();

    public static EnrichedMacAddressContextDetailsResponse create(MacAddressContextDetailsResponse context, MacAddressContextTypeResponse contextType, Dot11MonitoredNetworkContextResponse servesDot11MonitoredNetwork) {
        return builder()
                .context(context)
                .contextType(contextType)
                .servesDot11MonitoredNetwork(servesDot11MonitoredNetwork)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EnrichedMacAddressContextDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder context(MacAddressContextDetailsResponse context);

        public abstract Builder contextType(MacAddressContextTypeResponse contextType);

        public abstract Builder servesDot11MonitoredNetwork(Dot11MonitoredNetworkContextResponse servesDot11MonitoredNetwork);

        public abstract EnrichedMacAddressContextDetailsResponse build();
    }
}
