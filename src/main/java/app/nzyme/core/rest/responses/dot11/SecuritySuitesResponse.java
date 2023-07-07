package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SecuritySuitesResponse {

    @JsonProperty("pairwise_ciphers")
    public abstract String pairwiseCiphers();

    @JsonProperty("group_cipher")
    public abstract String groupCipher();

    @JsonProperty("key_management_modes")
    public abstract String keyManagementModes();

    @JsonProperty("identifier")
    public abstract String identifier();

    public static SecuritySuitesResponse create(String pairwiseCiphers, String groupCipher, String keyManagementModes, String identifier) {
        return builder()
                .pairwiseCiphers(pairwiseCiphers)
                .groupCipher(groupCipher)
                .keyManagementModes(keyManagementModes)
                .identifier(identifier)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SecuritySuitesResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder pairwiseCiphers(String pairwiseCiphers);

        public abstract Builder groupCipher(String groupCipher);

        public abstract Builder keyManagementModes(String keyManagementModes);

        public abstract Builder identifier(String identifier);

        public abstract SecuritySuitesResponse build();
    }
}
