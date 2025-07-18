package app.nzyme.core.rest.responses.ethernet.arp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ArpStatisticsBucketResponse {

    @JsonProperty("total_count")
    public abstract long totalCount();

    @JsonProperty("request_count")
    public abstract long requestCount();

    @JsonProperty("reply_count")
    public abstract long replyCount();

    @JsonProperty("request_to_reply_ratio")
    public abstract double requestToReplyRatio();

    @JsonProperty("gratuitous_request_count")
    public abstract long gratuitousRequestCount();

    @JsonProperty("gratuitous_reply_count")
    public abstract long gratuitousReplyCount();

    public static ArpStatisticsBucketResponse create(long totalCount, long requestCount, long replyCount, double requestToReplyRatio, long gratuitousRequestCount, long gratuitousReplyCount) {
        return builder()
                .totalCount(totalCount)
                .requestCount(requestCount)
                .replyCount(replyCount)
                .requestToReplyRatio(requestToReplyRatio)
                .gratuitousRequestCount(gratuitousRequestCount)
                .gratuitousReplyCount(gratuitousReplyCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ArpStatisticsBucketResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder totalCount(long totalCount);

        public abstract Builder requestCount(long requestCount);

        public abstract Builder replyCount(long replyCount);

        public abstract Builder requestToReplyRatio(double requestToReplyRatio);

        public abstract Builder gratuitousRequestCount(long gratuitousRequestCount);

        public abstract Builder gratuitousReplyCount(long gratuitousReplyCount);

        public abstract ArpStatisticsBucketResponse build();
    }
}
