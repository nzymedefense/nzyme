package app.nzyme.core.rest.responses.gnss;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class GNSSStringBucketResponse {

    @JsonProperty("gps")
    @Nullable
    public abstract String gps();

    @JsonProperty("glonass")
    @Nullable
    public abstract String glonass();

    @JsonProperty("beidou")
    @Nullable
    public abstract String beidou();

    @JsonProperty("galileo")
    @Nullable
    public abstract String galileo();

    public static GNSSStringBucketResponse create(String gps, String glonass, String beidou, String galileo) {
        return builder()
                .gps(gps)
                .glonass(glonass)
                .beidou(beidou)
                .galileo(galileo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSStringBucketResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder gps(String gps);

        public abstract Builder glonass(String glonass);

        public abstract Builder beidou(String beidou);

        public abstract Builder galileo(String galileo);

        public abstract GNSSStringBucketResponse build();
    }
}
