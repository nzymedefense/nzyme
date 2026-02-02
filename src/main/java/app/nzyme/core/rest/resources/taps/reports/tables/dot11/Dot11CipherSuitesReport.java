package app.nzyme.core.rest.resources.taps.reports.tables.dot11;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Dot11CipherSuitesReport {

    @JsonProperty("group_cipher")
    public abstract String groupCipher();

    @JsonProperty("pairwise_ciphers")
    public abstract List<String> pairwiseCiphers();

    @JsonProperty("key_management_modes")
    public abstract List<String> keyManagementModes();

    @JsonCreator
    public static Dot11CipherSuitesReport create(@JsonProperty("group_cipher") String groupCipher,
                                                 @JsonProperty("pairwise_ciphers") List<String> pairwiseCiphers,
                                                 @JsonProperty("key_management_modes") List<String> keyManagementModes) {
        return builder()
                .groupCipher(groupCipher)
                .pairwiseCiphers(pairwiseCiphers)
                .keyManagementModes(keyManagementModes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11CipherSuitesReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder groupCipher(String groupCipher);

        public abstract Builder pairwiseCiphers(List<String> pairwiseCiphers);

        public abstract Builder keyManagementModes(List<String> keyManagementModes);

        public abstract Dot11CipherSuitesReport build();
    }
}
