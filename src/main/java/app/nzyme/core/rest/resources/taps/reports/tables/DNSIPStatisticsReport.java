package app.nzyme.core.rest.resources.taps.reports.tables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DNSIPStatisticsReport {

    public abstract Long requestCount();
    public abstract Long requestBytes();
    public abstract Long responseCount();
    public abstract Long responseBytes();
    public abstract Long nxDomainCount();

    @JsonCreator
    public static DNSIPStatisticsReport create(@JsonProperty("request_count") Long requestCount,
                                               @JsonProperty("request_bytes") Long requestBytes,
                                               @JsonProperty("response_count") Long responseCount,
                                               @JsonProperty("response_bytes") Long responseBytes,
                                               @JsonProperty("nxdomain_count") Long nxDomainCount) {
        return builder()
                .requestCount(requestCount)
                .requestBytes(requestBytes)
                .responseCount(responseCount)
                .responseBytes(responseBytes)
                .nxDomainCount(nxDomainCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSIPStatisticsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder requestCount(Long requestCount);

        public abstract Builder requestBytes(Long requestBytes);

        public abstract Builder responseCount(Long responseCount);

        public abstract Builder responseBytes(Long responseBytes);

        public abstract Builder nxDomainCount(Long nxDomainCount);

        public abstract DNSIPStatisticsReport build();
    }

}
