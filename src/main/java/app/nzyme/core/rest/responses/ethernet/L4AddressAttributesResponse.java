package app.nzyme.core.rest.responses.ethernet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class L4AddressAttributesResponse {

    @JsonProperty("is_site_local")
    public abstract boolean isSiteLocal();

    @JsonProperty("is_loopback")
    public abstract boolean isLoopback();

    @JsonProperty("is_multicast")
    public abstract boolean isMulticast();

    public static L4AddressAttributesResponse create(boolean isSiteLocal, boolean isLoopback, boolean isMulticast) {
        return builder()
                .setSiteLocal(isSiteLocal)
                .setLoopback(isLoopback)
                .setMulticast(isMulticast)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_L4AddressAttributesResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setSiteLocal(boolean newSiteLocal);

        public abstract Builder setLoopback(boolean newLoopback);

        public abstract Builder setMulticast(boolean newMulticast);

        public abstract L4AddressAttributesResponse build();
    }
}
