package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ActivateGeoIpProviderRequest {

    public abstract String providerName();

    @JsonCreator
    public static ActivateGeoIpProviderRequest create(@JsonProperty("provider_name") String providerName) {
        return builder()
                .providerName(providerName)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ActivateGeoIpProviderRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder providerName(String providerName);

        public abstract ActivateGeoIpProviderRequest build();
    }
}
