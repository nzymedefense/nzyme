package app.nzyme.core.rest.responses.taps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TapHighLevelInformationListResponse {

    @JsonProperty("taps")
    public abstract List<TapHighLevelInformationDetailsResponse> taps();

    public static TapHighLevelInformationListResponse create(List<TapHighLevelInformationDetailsResponse> taps) {
        return builder()
                .taps(taps)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapHighLevelInformationListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder taps(List<TapHighLevelInformationDetailsResponse> taps);

        public abstract TapHighLevelInformationListResponse build();
    }
}
