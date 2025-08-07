package app.nzyme.core.rest.responses.gnss;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class GNSSDoubleBucketResponse {

    @JsonProperty("gps")
    @Nullable
    public abstract Double gps();
    @JsonProperty("glonass")
    @Nullable
    public abstract Double glonass();
    @JsonProperty("beidou")
    @Nullable
    public abstract Double beidou();
    @JsonProperty("galileo")
    @Nullable
    public abstract Double galileo();

    public static GNSSDoubleBucketResponse create(Double gps, Double glonass, Double beidou, Double galileo) {
        return builder()
                .gps(gps)
                .glonass(glonass)
                .beidou(beidou)
                .galileo(galileo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSDoubleBucketResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder gps(Double gps);

        public abstract Builder glonass(Double glonass);

        public abstract Builder beidou(Double beidou);

        public abstract Builder galileo(Double galileo);

        public abstract GNSSDoubleBucketResponse build();
    }

}
