package app.nzyme.core.rest.responses.dot11.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ClientActivityHistogramValueResponse {

    @JsonProperty("bucket")
    public abstract DateTime bucket();

    @JsonProperty("total_frames")
    public abstract long totalFrames();

    @JsonProperty("connected_frames")
    public abstract long connectedFrames();

    @JsonProperty("disconnected_frames")
    public abstract long disconnectedFrames();

    @JsonProperty("disconnection_activity")
    public abstract long disconnectionActivity();

    public static ClientActivityHistogramValueResponse create(DateTime bucket, long totalFrames, long connectedFrames, long disconnectedFrames, long disconnectionActivity) {
        return builder()
                .bucket(bucket)
                .totalFrames(totalFrames)
                .connectedFrames(connectedFrames)
                .disconnectedFrames(disconnectedFrames)
                .disconnectionActivity(disconnectionActivity)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientActivityHistogramValueResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder totalFrames(long totalFrames);

        public abstract Builder connectedFrames(long connectedFrames);

        public abstract Builder disconnectedFrames(long disconnectedFrames);

        public abstract Builder disconnectionActivity(long disconnectionActivity);

        public abstract ClientActivityHistogramValueResponse build();
    }
}
