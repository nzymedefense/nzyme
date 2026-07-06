package app.nzyme.core.rest.responses.gnss;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class GNSSTapListResponse {

    @JsonProperty("taps")
    public abstract List<GNSSTapDetailsResponse> taps();

    public static GNSSTapListResponse create(List<GNSSTapDetailsResponse> taps) {
        return builder()
                .taps(taps)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSTapListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder taps(List<GNSSTapDetailsResponse> taps);

        public abstract GNSSTapListResponse build();
    }
}
