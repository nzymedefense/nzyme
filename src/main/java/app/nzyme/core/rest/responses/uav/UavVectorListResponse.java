package app.nzyme.core.rest.responses.uav;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class UavVectorListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("vectors")
    public abstract List<UavVectorDetailsResponse> vectors();

    public static UavVectorListResponse create(long count, List<UavVectorDetailsResponse> vectors) {
        return builder()
                .count(count)
                .vectors(vectors)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavVectorListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder vectors(List<UavVectorDetailsResponse> vectors);

        public abstract UavVectorListResponse build();
    }
}
