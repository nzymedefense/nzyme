package horse.wtf.nzyme.rest.responses.ethernet.dns;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class DNSStatisticsResponse {

    @JsonProperty("buckets")
    public abstract Map<DateTime, DNSStatisticsBucketResponse> buckets();

    @JsonProperty("traffic_summary")
    public abstract DNSTrafficSummaryResponse trafficSummary();

    @JsonProperty("pair_summary")
    public abstract List<DNSPairSummaryResponse> pairSummary();

    public static DNSStatisticsResponse create(Map<DateTime, DNSStatisticsBucketResponse> buckets, DNSTrafficSummaryResponse trafficSummary, List<DNSPairSummaryResponse> pairSummary) {
        return builder()
                .buckets(buckets)
                .trafficSummary(trafficSummary)
                .pairSummary(pairSummary)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSStatisticsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder buckets(Map<DateTime, DNSStatisticsBucketResponse> buckets);

        public abstract Builder trafficSummary(DNSTrafficSummaryResponse trafficSummary);

        public abstract Builder pairSummary(List<DNSPairSummaryResponse> pairSummary);

        public abstract DNSStatisticsResponse build();
    }

}
