package app.nzyme.core.rest.responses.gnss;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class GNSSIntegerBucketResponse {

    @JsonProperty("gps")
    @Nullable
    public abstract Integer gps();

    @JsonProperty("glonass")
    @Nullable
    public abstract Integer glonass();

    @JsonProperty("beidou")
    @Nullable
    public abstract Integer beidou();

    @JsonProperty("galileo")
    @Nullable
    public abstract Integer galileo();

    public static GNSSIntegerBucketResponse create(Integer gps, Integer glonass, Integer beidou, Integer galileo) {
        return builder()
                .gps(gps)
                .glonass(glonass)
                .beidou(beidou)
                .galileo(galileo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSIntegerBucketResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder gps(Integer gps);

        public abstract Builder glonass(Integer glonass);

        public abstract Builder beidou(Integer beidou);

        public abstract Builder galileo(Integer galileo);

        public abstract GNSSIntegerBucketResponse build();
    }
}
