package app.nzyme.core.dot11.monitoring;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class Dot11BanditDescription {

    public abstract String name();
    public abstract String description();

    // Nullable for bandits that are not detected by fingerprint, like the Pwnagotchi.
    @Nullable
    public abstract String fingerprint();

    public static Dot11BanditDescription create(String name, String description, String fingerprint) {
        return builder()
                .name(name)
                .description(description)
                .fingerprint(fingerprint)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11BanditDescription.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder fingerprint(String fingerprint);

        public abstract Dot11BanditDescription build();
    }
}
