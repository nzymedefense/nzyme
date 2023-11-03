package app.nzyme.core.dot11;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Dot11MacAddressMetadata {

    public abstract Dot11MacAddressType type();

    public static Dot11MacAddressMetadata create(Dot11MacAddressType type) {
        return builder()
                .type(type)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11MacAddressMetadata.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(Dot11MacAddressType type);

        public abstract Dot11MacAddressMetadata build();
    }
}
