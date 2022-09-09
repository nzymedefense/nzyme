package horse.wtf.nzyme.rest.resources.taps.reports.tables;

import app.nzyme.plugin.retro.dns.rest.DNSRetroQueryLogReport;
import app.nzyme.plugin.retro.dns.rest.DNSRetroResponseLogReport;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class DNSTablesReport {

    public abstract Map<String, DNSIPStatisticsReport> ips();
    public abstract List<DNSNxDomainLogReport> nxdomains();

    public abstract List<DNSEntropyLogReport> entropyLog();

    public abstract Map<String, Map<String, Long>> pairs();
    public abstract List<DNSRetroQueryLogReport> retroQueries();

    public abstract List<DNSRetroResponseLogReport> retroResponses();

    @JsonCreator
    public static DNSTablesReport create(@JsonProperty("ips") Map<String, DNSIPStatisticsReport> ips,
                                         @JsonProperty("nxdomains") List<DNSNxDomainLogReport> nxdomains,
                                         @JsonProperty("entropy_log") List<DNSEntropyLogReport> entropyLog,
                                         @JsonProperty("pairs") Map<String, Map<String, Long>> pairs,
                                         @JsonProperty("retro_queries") List<DNSRetroQueryLogReport> retroQueries,
                                         @JsonProperty("retro_responses") List<DNSRetroResponseLogReport> retroResponses) {
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

        public abstract Builder retroQueries(List<DNSRetroQueryLogReport> retroQueries);

        public abstract Builder retroResponses(List<DNSRetroResponseLogReport> retroResponses);

        public abstract DNSTablesReport build();
    }

}
