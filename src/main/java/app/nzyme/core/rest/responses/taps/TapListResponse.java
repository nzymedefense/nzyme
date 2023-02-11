package app.nzyme.core.rest.responses.taps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TapListResponse {

    @JsonProperty("count")
    public abstract int count();

    @JsonProperty("taps")
    public abstract List<TapDetailsResponse> taps();

    public static TapListResponse create(int count, List<TapDetailsResponse> taps) {
        return builder()
                .count(count)
                .taps(taps)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(int count);

        public abstract Builder taps(List<TapDetailsResponse> taps);

        public abstract TapListResponse build();
    }

}
