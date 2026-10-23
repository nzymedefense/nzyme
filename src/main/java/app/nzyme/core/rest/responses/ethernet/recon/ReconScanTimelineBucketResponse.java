package app.nzyme.core.rest.responses.ethernet.recon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ReconScanTimelineBucketResponse {

    @JsonProperty("open")
    public abstract long open();

    @JsonProperty("closed")
    public abstract long closed();

    @JsonProperty("unanswered")
    public abstract long unanswered();

    public static ReconScanTimelineBucketResponse create(long open, long closed, long unanswered) {
        return builder()
                .open(open)
                .closed(closed)
                .unanswered(unanswered)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconScanTimelineBucketResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder open(long open);
        public abstract Builder closed(long closed);
        public abstract Builder unanswered(long unanswered);
        public abstract ReconScanTimelineBucketResponse build();
    }
}
