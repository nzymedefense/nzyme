package app.nzyme.core.ethernet;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class L4AddressAttributes {

    public abstract boolean isSiteLocal();
    public abstract boolean isLoopback();
    public abstract boolean isMulticast();

    public static L4AddressAttributes create(boolean isSiteLocal, boolean isLoopback, boolean isMulticast) {
        return builder()
                .setSiteLocal(isSiteLocal)
                .setLoopback(isLoopback)
                .setMulticast(isMulticast)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_L4AddressAttributes.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setSiteLocal(boolean newSiteLocal);

        public abstract Builder setLoopback(boolean newLoopback);

        public abstract Builder setMulticast(boolean newMulticast);

        public abstract L4AddressAttributes build();
    }
}
