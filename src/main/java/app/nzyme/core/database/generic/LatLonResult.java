package app.nzyme.core.database.generic;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class LatLonResult {

    public abstract double lat();
    public abstract double lon();

    public static LatLonResult create(double lat, double lon) {
        return builder()
                .lat(lat)
                .lon(lon)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_LatLonResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder lat(double lat);

        public abstract Builder lon(double lon);

        public abstract LatLonResult build();
    }
}
