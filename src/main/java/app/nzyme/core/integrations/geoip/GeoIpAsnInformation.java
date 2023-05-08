package app.nzyme.core.integrations.geoip;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class GeoIpAsnInformation {

    @Nullable
    public abstract Long number();

    @Nullable
    public abstract String name();

    @Nullable
    public abstract String domain();

    public static GeoIpAsnInformation create(Long number, String name, String domain) {
        return builder()
                .number(number)
                .name(name)
                .domain(domain)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GeoIpAsnInformation.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder number(Long number);

        public abstract Builder name(String name);

        public abstract Builder domain(String domain);

        public abstract GeoIpAsnInformation build();
    }
}
