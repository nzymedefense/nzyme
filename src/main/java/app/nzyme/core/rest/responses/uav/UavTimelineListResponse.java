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

    @JsonProperty("timelines")
    public abstract Map<UUID, List<UavTimelineDetailsResponse>> timelines();

    public static UavTimelineListResponse create(long count, Map<UUID, List<UavTimelineDetailsResponse>> timelines) {
        return builder()
                .count(count)
                .timelines(timelines)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavTimelineListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder timelines(Map<UUID, List<UavTimelineDetailsResponse>> timelines);

        public abstract UavTimelineListResponse build();
    }
}
