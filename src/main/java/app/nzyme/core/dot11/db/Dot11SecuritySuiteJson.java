package app.nzyme.core.dot11.db;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Dot11SecuritySuiteJson {

    public abstract String pairwiseCiphers();
    public abstract String groupCipher();
    public abstract String keyManagementModes();

    @JsonCreator
    public static Dot11SecuritySuiteJson create(@JsonProperty("pairwise_ciphers") String pairwiseCiphers,
                                                @JsonProperty("group_cipher") String groupCipher,
                                                @JsonProperty("key_management_modes") String keyManagementModes) {
        return builder()
                .pairwiseCiphers(pairwiseCiphers)
                .groupCipher(groupCipher)
                .keyManagementModes(keyManagementModes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11SecuritySuiteJson.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder pairwiseCiphers(String pairwiseCiphers);

        public abstract Builder groupCipher(String groupCipher);

        public abstract Builder keyManagementModes(String keyManagementModes);

        public abstract Dot11SecuritySuiteJson build();
    }
}
