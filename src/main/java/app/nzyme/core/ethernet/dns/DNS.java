package app.nzyme.core.ethernet.dns;

import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.dns.db.DNSPairSummary;
import app.nzyme.core.ethernet.dns.db.DNSStatisticsBucket;
import app.nzyme.core.ethernet.dns.db.DNSTrafficSummary;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DNS {

    private final Ethernet ethernet;

    public DNS(Ethernet ethernet) {
        this.ethernet = ethernet;
    }

    public List<DNSStatisticsBucket> getStatistics(TimeRange timeRange,
                                                   Bucketing.BucketingConfiguration bucketing,
                                                   List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return ethernet.getNzyme().getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, created_at) AS bucket, " +
                                "SUM(request_count) AS request_count, SUM(request_bytes) AS request_bytes, " +
                                "SUM(response_count) AS response_count, SUM(response_bytes) AS response_bytes, " +
                                "SUM(nxdomain_count) AS nxdomain_count FROM dns_statistics " +
                                "WHERE created_at >= :tr_from AND created_at <= :tr_to AND tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(DNSStatisticsBucket.class)
                        .list()
        );
    }

    public DNSTrafficSummary getTrafficSummary(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return DNSTrafficSummary.create(0,0,0);
        }

        return ethernet.getNzyme().getDatabase().withHandle(handle ->
                handle.createQuery("SELECT (SUM(request_count)+SUM(response_count)) AS total_dns_packets, " +
                                "(SUM(request_bytes)+SUM(response_bytes)) AS total_dns_traffic_bytes, " +
                                "SUM(nxdomain_count) AS nxdomain_count " +
                                "FROM dns_statistics WHERE created_at >= :tr_from AND created_at <= :tr_to " +
                                "AND tap_uuid IN (<taps>)")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(DNSTrafficSummary.class)
                        .one()
        );
    }

    public List<DNSPairSummary> getPairSummary(TimeRange timeRange, int limit, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return ethernet.getNzyme().getDatabase().withHandle(handle ->
                handle.createQuery("SELECT server, server_geo_asn_number, server_geo_asn_name, " +
                                "server_geo_asn_domain, server_geo_country_code, SUM(count) AS request_count, " +
                                "COUNT(DISTINCT(ip)) AS client_count FROM dns_pairs " +
                                "WHERE created_at >= :tr_from AND created_at <= :tr_to " +
                                "AND tap_uuid IN (<taps>) AND server <> '224.0.0.251' " +
                                "GROUP BY server, server_geo_asn_number, server_geo_asn_name, " +
                                "server_geo_asn_domain, server_geo_country_code " +
                                "ORDER BY request_count " +
                                "DESC LIMIT :limit")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .mapTo(DNSPairSummary.class)
                        .list()
        );
    }

}
