package app.nzyme.core.dot11.bandits;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class Dot11BanditDescription {

    public abstract String id();
    public abstract boolean isCustom();
    public abstract String name();
    public abstract String description();

    // Nullable for bandits that are not detected by fingerprint, like the Pwnagotchi.
    @Nullable
    public abstract List<String> fingerprints();

    public static Dot11BanditDescription create(String id, boolean isCustom, String name, String description, List<String> fingerprints) {
        return builder()
                .id(id)
                .isCustom(isCustom)
                .name(name)
                .description(description)
                .fingerprints(fingerprints)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11BanditDescription.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Builder isCustom(boolean isCustom);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Dot11BanditDescription build();
    }
}
