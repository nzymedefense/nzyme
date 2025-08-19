package app.nzyme.core.gnss.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class GNSSConstellationDistances {

    @Nullable
    public abstract Double gps();
    @Nullable
    public abstract Double glonass();
    @Nullable
    public abstract Double beidou();
    @Nullable
    public abstract Double galileo();

    public static GNSSConstellationDistances create(Double gps, Double glonass, Double beidou, Double galileo) {
        return builder()
                .gps(gps)
                .glonass(glonass)
                .beidou(beidou)
                .galileo(galileo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSConstellationDistances.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder gps(Double gps);

        public abstract Builder glonass(Double glonass);

        public abstract Builder beidou(Double beidou);

        public abstract Builder galileo(Double galileo);

        public abstract GNSSConstellationDistances build();
    }

}
