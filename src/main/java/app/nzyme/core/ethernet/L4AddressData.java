package app.nzyme.core.ethernet;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class L4AddressData {

    @Nullable // Some higher-level protocols don't record a MAC.
    public abstract String mac();

    public abstract String address();
    public abstract int port();

    @Nullable
    public abstract GeoData geo();

    @Nullable
    public abstract L4AddressAttributes attributes();

    public static L4AddressData create(String mac, String address, int port, GeoData geo, L4AddressAttributes attributes) {
        return builder()
                .mac(mac)
                .address(address)
                .port(port)
                .geo(geo)
                .attributes(attributes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_L4AddressData.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(String mac);

        public abstract Builder address(String address);

        public abstract Builder port(int port);

        public abstract Builder geo(GeoData geo);

        public abstract Builder attributes(L4AddressAttributes attributes);

        public abstract L4AddressData build();
    }
}
