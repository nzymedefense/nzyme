package horse.wtf.nzyme.configuration;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Dot11BSSIDDefinition {

    public abstract String address();
    public abstract String fingerprint();

    public static Dot11BSSIDDefinition create(String address, String fingerprint) {
        return builder()
                .address(address)
                .fingerprint(fingerprint)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11BSSIDDefinition.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder address(String address);

        public abstract Builder fingerprint(String fingerprint);

        public abstract Dot11BSSIDDefinition build();
    }

}
