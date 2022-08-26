package horse.wtf.nzyme.rest.responses.ethernet.dns;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.Map;

@AutoValue
public abstract class DNSStatisticsResponse {

    @JsonProperty("buckets")
    public abstract Map<DateTime, DNSStatisticsBucketResponse> buckets();

    public static DNSStatisticsResponse create(Map<DateTime, DNSStatisticsBucketResponse> buckets) {
        return builder()
                .buckets(buckets)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSStatisticsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder buckets(Map<DateTime, DNSStatisticsBucketResponse> buckets);

        public abstract DNSStatisticsResponse build();
    }

}
