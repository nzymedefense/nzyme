package app.nzyme.core.integrations.geoip;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class GeoIpGeoInformation {

    @Nullable
    public abstract String city();

    @Nullable
    public abstract String countryCode();

    @Nullable
    public abstract String countryName();

    @Nullable
    public abstract Float latitude();

    @Nullable
    public abstract Float longitude();

    public static GeoIpGeoInformation create(String city, String countryCode, String countryName, Float latitude, Float longitude) {
        return builder()
                .city(city)
                .countryCode(countryCode)
                .countryName(countryName)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GeoIpGeoInformation.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder city(String city);

        public abstract Builder countryCode(String countryCode);

        public abstract Builder countryName(String countryName);

        public abstract Builder latitude(Float latitude);

        public abstract Builder longitude(Float longitude);

        public abstract GeoIpGeoInformation build();
    }
}
