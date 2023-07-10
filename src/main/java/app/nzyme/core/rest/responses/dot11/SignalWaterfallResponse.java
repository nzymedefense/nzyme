package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class SignalWaterfallResponse {

    @JsonProperty("z")
    public abstract List<List<Long>> z();

    @JsonProperty("x")
    public abstract List<Integer> x();

    @JsonProperty("y")
    public abstract List<DateTime> y();

    @JsonProperty("tracks")
    public abstract List<SignalWaterfallTrackResponse> tracks();

    public static SignalWaterfallResponse create(List<List<Long>> z, List<Integer> x, List<DateTime> y, List<SignalWaterfallTrackResponse> tracks) {
        return builder()
                .z(z)
                .x(x)
                .y(y)
                .tracks(tracks)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SignalWaterfallResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder z(List<List<Long>> z);

        public abstract Builder x(List<Integer> x);

        public abstract Builder y(List<DateTime> y);

        public abstract Builder tracks(List<SignalWaterfallTrackResponse> tracks);

        public abstract SignalWaterfallResponse build();
    }
}
