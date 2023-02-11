package app.nzyme.core.rest.responses.ethernet.dns;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DNSStatisticsBucketResponse {

    @JsonProperty("bucket")
    public abstract DateTime bucket();

    @JsonProperty("request_count")
    public abstract Long requestCount();

    @JsonProperty("request_bytes")
    public abstract Long requestBytes();

    @JsonProperty("response_count")
    public abstract Long responseCount();

    @JsonProperty("response_bytes")
    public abstract Long responseBytes();

    @JsonProperty("nxdomain_count")
    public abstract Long nxdomainCount();

    public static DNSStatisticsBucketResponse create(DateTime bucket, Long requestCount, Long requestBytes, Long responseCount, Long responseBytes, Long nxdomainCount) {
        return builder()
                .bucket(bucket)
                .requestCount(requestCount)
                .requestBytes(requestBytes)
                .responseCount(responseCount)
                .responseBytes(responseBytes)
                .nxdomainCount(nxdomainCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSStatisticsBucketResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder requestCount(Long requestCount);

        public abstract Builder requestBytes(Long requestBytes);

        public abstract Builder responseCount(Long responseCount);

        public abstract Builder responseBytes(Long responseBytes);

        public abstract Builder nxdomainCount(Long nxdomainCount);

        public abstract DNSStatisticsBucketResponse build();
    }
}
