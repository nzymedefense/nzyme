package horse.wtf.nzyme.configuration;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Dot11BSSIDDefinition {

    public abstract String address();
    public abstract List<String> fingerprints();
    public abstract ExpectedSignalStrength expectedSignalStrength();

    public static Dot11BSSIDDefinition create(String address, List<String> fingerprints, ExpectedSignalStrength expectedSignalStrength) {
        return builder()
                .address(address)
                .fingerprints(fingerprints)
                .expectedSignalStrength(expectedSignalStrength)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11BSSIDDefinition.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder address(String address);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Builder expectedSignalStrength(ExpectedSignalStrength expectedSignalStrength);

        public abstract Dot11BSSIDDefinition build();
    }

}
