package app.nzyme.core.registry;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RegistryEntry {

    public abstract String key();
    public abstract String value();

    public static RegistryEntry create(String key, String value) {
        return builder()
                .key(key)
                .value(value)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_RegistryEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder key(String key);

        public abstract Builder value(String value);

        public abstract RegistryEntry build();
    }
}
