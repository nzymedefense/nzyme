package app.nzyme.core.rest.responses.uav;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class UavTimelineListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("timeline")
    public abstract List<UavTimelineDetailsResponse> timeline();

    public static UavTimelineListResponse create(long count, List<UavTimelineDetailsResponse> timeline) {
        return builder()
                .count(count)
                .timeline(timeline)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavTimelineListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder timeline(List<UavTimelineDetailsResponse> timeline);

        public abstract UavTimelineListResponse build();
    }
}
