package app.nzyme.core.ethernet.dns;

import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.dns.db.DNSPairSummary;
import app.nzyme.core.ethernet.dns.db.DNSStatisticsBucket;
import app.nzyme.core.ethernet.dns.db.DNSTrafficSummary;
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

    public List<DNSStatisticsBucket> getStatistics(int hours, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return ethernet.getNzyme().getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc('MINUTE', created_at) AS bucket, " +
                                "SUM(request_count) AS request_count, SUM(request_bytes) AS request_bytes, " +
                                "SUM(response_count) AS response_count, SUM(response_bytes) AS response_bytes, " +
                                "SUM(nxdomain_count) AS nxdomain_count FROM dns_statistics " +
                                "WHERE created_at > :created_at AND tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bindList("taps", taps)
                        .bind("created_at", DateTime.now().minusHours(hours))
                        .mapTo(DNSStatisticsBucket.class)
                        .list()
        );
    }

    public DNSTrafficSummary getTrafficSummary(int hours, List<UUID> taps) {
        if (taps.isEmpty()) {
            return DNSTrafficSummary.create(0,0,0);
        }

        return ethernet.getNzyme().getDatabase().withHandle(handle ->
                handle.createQuery("SELECT (SUM(request_count)+SUM(response_count)) AS total_dns_packets, " +
                                "(SUM(request_bytes)+SUM(response_bytes)) AS total_dns_traffic_bytes, " +
                                "SUM(nxdomain_count) AS nxdomain_count " +
                                "FROM dns_statistics WHERE created_at > :created_at AND tap_uuid IN (<taps>)")
                        .bindList("taps", taps)
                        .bind("created_at", DateTime.now().minusHours(hours))
                        .mapTo(DNSTrafficSummary.class)
                        .one()
        );
    }

    public List<DNSPairSummary> getPairSummary(int hours, int limit, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return ethernet.getNzyme().getDatabase().withHandle(handle ->
                handle.createQuery("SELECT server, SUM(count) AS request_count, COUNT(DISTINCT(ip)) AS client_count " +
                                "FROM dns_pairs WHERE created_at > :created_at AND tap_uuid IN (<taps>) " +
                                "GROUP BY server ORDER BY request_count DESC LIMIT :limit")
                        .bindList("taps", taps)
                        .bind("created_at", DateTime.now().minusHours(hours))
                        .bind("limit", limit)
                        .mapTo(DNSPairSummary.class)
                        .list()
        );
    }

}
