package app.nzyme.core.ethernet.dns;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.dns.db.*;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import com.google.common.collect.Lists;
import org.jdbi.v3.core.Handle;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DNS {

    private final NzymeNode nzyme;

    public DNS(Ethernet ethernet) {
        this.nzyme = ethernet.getNzyme();
    }

    public List<DNSStatisticsBucket> getStatistics(TimeRange timeRange,
                                                   Bucketing.BucketingConfiguration bucketing,
                                                   List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
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

        return nzyme.getDatabase().withHandle(handle ->
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

    public long countPairs(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM(SELECT server_address, server_geo_asn_number, " +
                                "server_geo_asn_name, server_geo_asn_domain, server_geo_country_code, " +
                                "SUM(count) AS request_count, COUNT(DISTINCT(client_address)) AS client_count " +
                                "FROM dns_pairs WHERE created_at >= :tr_from AND created_at <= :tr_to " +
                                "AND tap_uuid IN (<taps>) AND server_address <> '224.0.0.251' " +
                                "GROUP BY server_address, server_port, server_geo_asn_number, server_geo_asn_name, " +
                                "server_geo_asn_domain, server_geo_country_code " +
                                "ORDER BY request_count) AS x")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<DNSPairSummary> getPairs(TimeRange timeRange, int limit, int offset, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT server_address, server_port, server_geo_asn_number, " +
                                "server_geo_asn_name, server_geo_asn_domain, server_geo_country_code, " +
                                "SUM(count) AS request_count, COUNT(DISTINCT(client_address)) AS client_count " +
                                "FROM dns_pairs WHERE created_at >= :tr_from AND created_at <= :tr_to " +
                                "AND tap_uuid IN (<taps>) AND server_address <> '224.0.0.251' " +
                                "GROUP BY server_address, server_port, server_geo_asn_number, server_geo_asn_name, " +
                                "server_geo_asn_domain, server_geo_country_code " +
                                "ORDER BY request_count DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(DNSPairSummary.class)
                        .list()
        );
    }

    public long countAllEntropyLogs(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM dns_entropy_log " +
                                "WHERE timestamp >= :tr_from AND timestamp <= :tr_to AND tap_uuid IN (<taps>)")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<DNSEntropyLogEntry> findAllEntropyLogs(TimeRange timeRange, int limit, int offset, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM dns_entropy_log " +
                                "WHERE timestamp >= :tr_from AND timestamp <= :tr_to AND tap_uuid IN (<taps>) " +
                                "ORDER BY timestamp DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(DNSEntropyLogEntry.class)
                        .list()
        );
    }

    public Optional<DNSTransaction> findDNSTransaction(int transactionId,
                                                       DateTime transactionTimestamp,
                                                       List<UUID> taps) {
        return findDNSTransaction(transactionId, transactionTimestamp, taps, null);
    }

    public Optional<DNSTransaction> findDNSTransaction(int transactionId,
                                                       DateTime transactionTimestamp,
                                                       List<UUID> taps,
                                                       @Nullable Handle existingHandle) {
        if (existingHandle != null) {
            return findDNSTransactionWithHandle(transactionId, transactionTimestamp, taps, existingHandle);
        } else {
            return nzyme.getDatabase().withHandle(handle ->
                    findDNSTransactionWithHandle(transactionId, transactionTimestamp, taps, handle)
            );
        }
    }

    private Optional<DNSTransaction> findDNSTransactionWithHandle(int transactionId,
                                                                  DateTime transactionTimestamp,
                                                                  List<UUID> taps,
                                                                  Handle handle) {
        List<DNSLogEntry> logs = handle.createQuery("SELECT * FROM dns_log " +
                        "WHERE transaction_id = :transaction_id AND timestamp >= :tr_from " +
                        "AND timestamp <= :tr_to AND tap_uuid IN (<taps>) " +
                        "ORDER BY data_type, data_value")
                .bind("transaction_id", transactionId)
                .bind("tr_from", transactionTimestamp.minusMinutes(1))
                .bind("tr_to", transactionTimestamp.plusMinutes(1))
                .bindList("taps", taps)
                .mapTo(DNSLogEntry.class)
                .list();

        DNSLogEntry query = null;
        List<DNSLogEntry> responses = Lists.newArrayList();
        for (DNSLogEntry log : logs) {
            switch (log.dnsType()) {
                case "query":
                    query = log;
                    break;
                case "response":
                    responses.add(log);
                    break;
            }
        }

        if (query == null) {
            // We may have missed the query of this transaction.
            return Optional.empty();
        }

        return Optional.of(DNSTransaction.create(query, responses));
    }

}
