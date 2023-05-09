package app.nzyme.core.rest.responses.integrations.geoip;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class GeoIpSummaryResponse {

    @JsonProperty("active_provider")
    public abstract String activeProvider();

    public static GeoIpSummaryResponse create(String activeProvider) {
        return builder()
                .activeProvider(activeProvider)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GeoIpSummaryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder activeProvider(String activeProvider);

        public abstract GeoIpSummaryResponse build();
    }
}
