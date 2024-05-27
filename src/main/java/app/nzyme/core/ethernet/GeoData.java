package app.nzyme.core.ethernet;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class GeoData {

    @Nullable
    public abstract Integer asnNumber();
    @Nullable
    public abstract String asnName();
    @Nullable
    public abstract String asnDomain();

    @Nullable
    public abstract String city();
    @Nullable
    public abstract String countryCode();
    @Nullable
    public abstract Float latitude();
    @Nullable
    public abstract Float longitude();

    public static GeoData create(Integer asnNumber, String asnName, String asnDomain, String city, String countryCode, Float latitude, Float longitude) {
        return builder()
                .asnNumber(asnNumber)
                .asnName(asnName)
                .asnDomain(asnDomain)
                .city(city)
                .countryCode(countryCode)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GeoData.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder asnNumber(Integer asnNumber);

        public abstract Builder asnName(String asnName);

        public abstract Builder asnDomain(String asnDomain);

        public abstract Builder city(String city);

        public abstract Builder countryCode(String countryCode);

        public abstract Builder latitude(Float latitude);

        public abstract Builder longitude(Float longitude);

        public abstract GeoData build();
    }
}
