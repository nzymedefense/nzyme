package app.nzyme.core.rest.resources.taps.reports.tables.dns;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class DnsTablesReport {

    public abstract Map<String, DnsIpStatisticsReport> ips();
    public abstract List<DnsEntropyLogReport> entropyLog();
    public abstract Map<String, Map<String, Long>> pairs();
    public abstract List<DnsLogReport> queryLog();
    public abstract List<DnsLogReport> responseLog();

    @JsonCreator
    public static DnsTablesReport create(@JsonProperty("ips") Map<String, DnsIpStatisticsReport> ips,
                                         @JsonProperty("entropy_log") List<DnsEntropyLogReport> entropyLog,
                                         @JsonProperty("pairs") Map<String, Map<String, Long>> pairs,
                                         @JsonProperty("queries") List<DnsLogReport> queryLog,
                                         @JsonProperty("responses") List<DnsLogReport> responseLog) {
        return builder()
                .ips(ips)
                .entropyLog(entropyLog)
                .pairs(pairs)
                .queryLog(queryLog)
                .responseLog(responseLog)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DnsTablesReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ips(Map<String, DnsIpStatisticsReport> ips);

        public abstract Builder entropyLog(List<DnsEntropyLogReport> entropyLog);

        public abstract Builder pairs(Map<String, Map<String, Long>> pairs);

        public abstract Builder queryLog(List<DnsLogReport> queryLog);

        public abstract Builder responseLog(List<DnsLogReport> responseLog);

        public abstract DnsTablesReport build();
    }

}
