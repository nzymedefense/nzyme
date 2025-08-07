package app.nzyme.core.gnss.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class GNSSIntegerBucket {

    public abstract DateTime bucket();

    @Nullable
    public abstract Integer gps();
    @Nullable
    public abstract Integer glonass();
    @Nullable
    public abstract Integer beidou();
    @Nullable
    public abstract Integer galileo();

    public static GNSSIntegerBucket create(DateTime bucket, Integer gps, Integer glonass, Integer beidou, Integer galileo) {
        return builder()
                .bucket(bucket)
                .gps(gps)
                .glonass(glonass)
                .beidou(beidou)
                .galileo(galileo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSIntegerBucket.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder gps(Integer gps);

        public abstract Builder glonass(Integer glonass);

        public abstract Builder beidou(Integer beidou);

        public abstract Builder galileo(Integer galileo);

        public abstract GNSSIntegerBucket build();
    }
}
