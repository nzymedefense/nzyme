package app.nzyme.core.gnss.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class GNSSDoubleBucket {

    public abstract DateTime bucket();

    @Nullable
    public abstract Double gps();
    @Nullable
    public abstract Double glonass();
    @Nullable
    public abstract Double beidou();
    @Nullable
    public abstract Double galileo();

    public static GNSSDoubleBucket create(DateTime bucket, Double gps, Double glonass, Double beidou, Double galileo) {
        return builder()
                .bucket(bucket)
                .gps(gps)
                .glonass(glonass)
                .beidou(beidou)
                .galileo(galileo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSDoubleBucket.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder gps(Double gps);

        public abstract Builder glonass(Double glonass);

        public abstract Builder beidou(Double beidou);

        public abstract Builder galileo(Double galileo);

        public abstract GNSSDoubleBucket build();
    }
}
