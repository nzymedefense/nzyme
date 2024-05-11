package app.nzyme.core.rest.resources.taps.reports.tables;

import app.nzyme.core.rest.resources.taps.reports.tables.dns.DNSLogReport;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class DNSTablesReport {

    public abstract Map<String, DNSIPStatisticsReport> ips();
    public abstract List<DNSEntropyLogReport> entropyLog();
    public abstract Map<String, Map<String, Long>> pairs();
    public abstract List<DNSLogReport> queryLog();
    public abstract List<DNSLogReport> responseLog();

    @JsonCreator
    public static DNSTablesReport create(@JsonProperty("ips") Map<String, DNSIPStatisticsReport> ips,
                                         @JsonProperty("entropy_log") List<DNSEntropyLogReport> entropyLog,
                                         @JsonProperty("pairs") Map<String, Map<String, Long>> pairs,
                                         @JsonProperty("queries") List<DNSLogReport> queryLog,
                                         @JsonProperty("responses") List<DNSLogReport> responseLog) {
        return builder()
                .ips(ips)
                .entropyLog(entropyLog)
                .pairs(pairs)
                .queryLog(queryLog)
                .responseLog(responseLog)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSTablesReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ips(Map<String, DNSIPStatisticsReport> ips);

        public abstract Builder entropyLog(List<DNSEntropyLogReport> entropyLog);

        public abstract Builder pairs(Map<String, Map<String, Long>> pairs);

        public abstract Builder queryLog(List<DNSLogReport> queryLog);

        public abstract Builder responseLog(List<DNSLogReport> responseLog);

        public abstract DNSTablesReport build();
    }

}
