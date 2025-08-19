package app.nzyme.core.rest.responses.gnss;

import app.nzyme.core.rest.responses.taps.TapHighLevelInformationDetailsResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class GNSSConstellationDistancesResponse {

    @JsonProperty("tap")
    public abstract TapHighLevelInformationDetailsResponse tap();

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

    public static GNSSConstellationDistancesResponse create(TapHighLevelInformationDetailsResponse tap, Double gps, Double glonass, Double beidou, Double galileo) {
        return builder()
                .tap(tap)
                .gps(gps)
                .glonass(glonass)
                .beidou(beidou)
                .galileo(galileo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSConstellationDistancesResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tap(TapHighLevelInformationDetailsResponse tap);

        public abstract Builder gps(Double gps);

        public abstract Builder glonass(Double glonass);

        public abstract Builder beidou(Double beidou);

        public abstract Builder galileo(Double galileo);

        public abstract GNSSConstellationDistancesResponse build();
    }
}
