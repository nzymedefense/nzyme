package app.nzyme.core.rest.responses.gnss;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GNSSTapLocationResponse {

    @JsonProperty("lat")
    public abstract Double lat();
    @JsonProperty("lon")
    public abstract Double lon();

    @JsonProperty("active")
    public abstract boolean active();
    @JsonProperty("name")
    public abstract String name();

    public static GNSSTapLocationResponse create(Double lat, Double lon, boolean active, String name) {
        return builder()
                .lat(lat)
                .lon(lon)
                .active(active)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSTapLocationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder lat(Double lat);

        public abstract Builder lon(Double lon);

        public abstract Builder active(boolean active);

        public abstract Builder name(String name);

        public abstract GNSSTapLocationResponse build();
    }
}
