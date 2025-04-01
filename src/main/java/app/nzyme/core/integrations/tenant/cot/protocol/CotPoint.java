package app.nzyme.core.integrations.tenant.cot.protocol;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CotPoint {

    @JacksonXmlProperty(isAttribute = true)
    public abstract double lat();

    @JacksonXmlProperty(isAttribute = true)
    public abstract double lon();

    @JacksonXmlProperty(isAttribute = true)
    public abstract double hae();

    @JacksonXmlProperty(isAttribute = true)
    public abstract double ce();

    @JacksonXmlProperty(isAttribute = true)
    public abstract double le();

    public static CotPoint create(double lat, double lon, double hae, double ce, double le) {
        return builder()
                .lat(lat)
                .lon(lon)
                .hae(hae)
                .ce(ce)
                .le(le)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CotPoint.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder lat(double lat);

        public abstract Builder lon(double lon);

        public abstract Builder hae(double hae);

        public abstract Builder ce(double ce);

        public abstract Builder le(double le);

        public abstract CotPoint build();
    }
}
