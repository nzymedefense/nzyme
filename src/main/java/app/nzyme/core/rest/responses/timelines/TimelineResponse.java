package app.nzyme.core.rest.responses.timelines;

import app.nzyme.core.rest.responses.taps.TapHighLevelInformationDetailsResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TimelineResponse {

    @JsonProperty("retention_days")
    public abstract int retentionDays();

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("events")
    public abstract List<TimelineEventDetailsResponse> events();

    @JsonProperty("ssids")
    public abstract List<String> ssids();

    @JsonProperty("fingerprints")
    public abstract List<String> fingerprints();

    @JsonProperty("taps")
    public abstract List<TapHighLevelInformationDetailsResponse> taps();

    public static TimelineResponse create(int retentionDays, long total, List<TimelineEventDetailsResponse> events, List<String> ssids, List<String> fingerprints, List<TapHighLevelInformationDetailsResponse> taps) {
        return builder()
                .retentionDays(retentionDays)
                .total(total)
                .events(events)
                .ssids(ssids)
                .fingerprints(fingerprints)
                .taps(taps)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimelineResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder retentionDays(int retentionDays);

        public abstract Builder total(long total);

        public abstract Builder events(List<TimelineEventDetailsResponse> events);

        public abstract Builder ssids(List<String> ssids);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Builder taps(List<TapHighLevelInformationDetailsResponse> taps);

        public abstract TimelineResponse build();
    }
}
