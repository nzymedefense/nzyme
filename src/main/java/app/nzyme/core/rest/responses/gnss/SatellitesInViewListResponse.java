package app.nzyme.core.rest.responses.gnss;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class SatellitesInViewListResponse {

    @JsonProperty("satellites")
    public abstract List<SatelliteInViewResponse> satellites();

    public static SatellitesInViewListResponse create(List<SatelliteInViewResponse> satellites) {
        return builder()
                .satellites(satellites)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SatellitesInViewListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder satellites(List<SatelliteInViewResponse> satellites);

        public abstract SatellitesInViewListResponse build();
    }
}
