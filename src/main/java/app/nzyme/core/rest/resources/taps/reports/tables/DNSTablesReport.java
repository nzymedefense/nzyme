package app.nzyme.core.rest.resources.taps.reports.tables;

import app.nzyme.core.rest.resources.taps.reports.tables.dns.DNSResponseLogReport;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import app.nzyme.core.rest.resources.taps.reports.tables.dns.DNSQueryLogReport;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class DNSTablesReport {

    public abstract Map<String, DNSIPStatisticsReport> ips();
    public abstract List<DNSNxDomainLogReport> nxdomains();

    public abstract List<DNSEntropyLogReport> entropyLog();

    public abstract Map<String, Map<String, Long>> pairs();
    public abstract List<DNSQueryLogReport> retroQueries();

    public abstract List<DNSResponseLogReport> retroResponses();

    @JsonCreator
    public static DNSTablesReport create(@JsonProperty("ips") Map<String, DNSIPStatisticsReport> ips,
                                         @JsonProperty("nxdomains") List<DNSNxDomainLogReport> nxdomains,
                                         @JsonProperty("entropy_log") List<DNSEntropyLogReport> entropyLog,
                                         @JsonProperty("pairs") Map<String, Map<String, Long>> pairs,
                                         @JsonProperty("retro_queries") List<DNSQueryLogReport> retroQueries,
                                         @JsonProperty("retro_responses") List<DNSResponseLogReport> retroResponses) {
        return builder()
                .ips(ips)
                .nxdomains(nxdomains)
                .entropyLog(entropyLog)
                .pairs(pairs)
                .retroQueries(retroQueries)
                .retroResponses(retroResponses)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSTablesReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ips(Map<String, DNSIPStatisticsReport> ips);

        public abstract Builder nxdomains(List<DNSNxDomainLogReport> nxdomains);

        public abstract Builder entropyLog(List<DNSEntropyLogReport> entropyLog);

        public abstract Builder pairs(Map<String, Map<String, Long>> pairs);

        public abstract Builder retroQueries(List<DNSQueryLogReport> retroQueries);

        public abstract Builder retroResponses(List<DNSResponseLogReport> retroResponses);

        public abstract DNSTablesReport build();
    }

}
