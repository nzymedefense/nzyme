package app.nzyme.core.dot11.db;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class Dot11SecuritySuiteJson {

    @Nullable
    public abstract String pairwiseCiphers();

    @Nullable
    public abstract String groupCipher();

    @Nullable
    public abstract String keyManagementModes();

    @Nullable
    public abstract String pmfMode();

    @JsonCreator
    public static Dot11SecuritySuiteJson create(@JsonProperty("pairwise_ciphers") String pairwiseCiphers,
                                                @JsonProperty("group_cipher") String groupCipher,
                                                @JsonProperty("key_management_modes") String keyManagementModes,
                                                @JsonProperty("pmf_mode") String pmfMode) {
        return builder()
                .pairwiseCiphers(pairwiseCiphers)
                .groupCipher(groupCipher)
                .keyManagementModes(keyManagementModes)
                .pmfMode(pmfMode)
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

        public abstract Builder pmfMode(String pmfMode);

        public abstract Dot11SecuritySuiteJson build();
    }
}
