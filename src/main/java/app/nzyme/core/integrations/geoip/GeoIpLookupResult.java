package app.nzyme.core.integrations.geoip;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class GeoIpLookupResult {

    @Nullable
    public abstract GeoIpAsnInformation asn();

    @Nullable
    public abstract GeoIpGeoInformation geo();

    public static GeoIpLookupResult create(GeoIpAsnInformation asn, GeoIpGeoInformation geo) {
        return builder()
                .asn(asn)
                .geo(geo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GeoIpLookupResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder asn(GeoIpAsnInformation asn);

        public abstract Builder geo(GeoIpGeoInformation geo);

        public abstract GeoIpLookupResult build();
    }
}
