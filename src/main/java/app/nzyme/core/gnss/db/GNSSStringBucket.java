package app.nzyme.core.gnss.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class GNSSStringBucket {

    public abstract DateTime bucket();

    @Nullable
    public abstract String gps();
    @Nullable
    public abstract String glonass();
    @Nullable
    public abstract String beidou();
    @Nullable
    public abstract String galileo();

    public static GNSSStringBucket create(DateTime bucket, String gps, String glonass, String beidou, String galileo) {
        return builder()
                .bucket(bucket)
                .gps(gps)
                .glonass(glonass)
                .beidou(beidou)
                .galileo(galileo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSStringBucket.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder gps(String gps);

        public abstract Builder glonass(String glonass);

        public abstract Builder beidou(String beidou);

        public abstract Builder galileo(String galileo);

        public abstract GNSSStringBucket build();
    }
}
