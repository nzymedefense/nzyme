package app.nzyme.core.ethernet.dns;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.generic.NumberBucketAggregationResult;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.dns.db.*;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Nullable;
import org.jdbi.v3.core.Handle;
import org.joda.time.DateTime;

import java.util.*;

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

    public Optional<DNSTransaction> findTransaction(int transactionId,
                                                    DateTime transactionTimestamp,
                                                    List<UUID> taps) {
        return findTransaction(transactionId, transactionTimestamp, taps, null);
    }

    public Optional<DNSTransaction> findTransaction(int transactionId,
                                                    DateTime transactionTimestamp,
                                                    List<UUID> taps,
                                                    @Nullable Handle existingHandle) {
        if (existingHandle != null) {
            return findTransactionWithHandle(transactionId, transactionTimestamp, taps, existingHandle);
        } else {
            return nzyme.getDatabase().withHandle(handle ->
                    findTransactionWithHandle(transactionId, transactionTimestamp, taps, handle)
            );
        }
    }

    public List<NumberBucketAggregationResult> getTransactionCountHistogram(String dnsType,
                                                                            TimeRange timeRange,
                                                                            Bucketing.BucketingConfiguration bucketing,
                                                                            List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, timestamp) AS bucket, COUNT(*) AS value " +
                                "FROM dns_log " +
                                "WHERE dns_type = :dns_type AND timestamp >= :tr_from AND timestamp <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bindList("taps", taps)
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("dns_type", dnsType)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(NumberBucketAggregationResult.class)
                        .list()
        );
    }

    private Optional<DNSTransaction> findTransactionWithHandle(int transactionId,
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

        DNSTransactionProcessingResult transaction;
        try {
            transaction = buildTransactionFromSingleTransactionLogs(logs);
        } catch (TransactionNotFoundException e) {
            return Optional.empty();
        }

        return Optional.of(DNSTransaction.create(transaction.query(), transaction.responses()));
    }

    public long countAllTransactions(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM dns_log " +
                                "WHERE dns_type = 'query' AND server_address <> '224.0.0.251' " +
                                "AND timestamp >= :tr_from AND timestamp <= :tr_to AND " +
                                "tap_uuid IN (<taps>)")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<DNSTransaction> findAllTransactions(TimeRange timeRange, int limit, int offset, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        List<DNSLogEntry> logs = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM dns_log " +
                                "WHERE timestamp >= :tr_from AND timestamp <= :tr_to AND tap_uuid IN (<taps>) " +
                                "AND server_address <> '224.0.0.251' " +
                                "ORDER BY timestamp DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(DNSLogEntry.class)
                        .list()
        );

        List<DNSTransaction> transactions = Lists.newArrayList();
        for (DNSTransactionProcessingResult transaction : buildTransactionFromManyTransactionLogs(logs)) {
            transactions.add(DNSTransaction.create(transaction.query(), transaction.responses()));
        }

        return transactions;
    }

    private DNSTransactionProcessingResult buildTransactionFromSingleTransactionLogs(List<DNSLogEntry> logs)
            throws TransactionNotFoundException {

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

        // We may have missed the query of this transaction.
        if (query == null) {
            throw new TransactionNotFoundException();
        }

        return DNSTransactionProcessingResult.create(query, responses);
    }

    private List<DNSTransactionProcessingResult> buildTransactionFromManyTransactionLogs(List<DNSLogEntry> logs) {
        // Group all transactions into a map.
        Map<Integer, List<DNSLogEntry>> transactions = Maps.newHashMap();
        for (DNSLogEntry log : logs) {
            if (!transactions.containsKey(log.transactionId())) {
                transactions.put(log.transactionId(), Lists.newArrayList());
            }

            transactions.get(log.transactionId()).add(log);
        }

        List<DNSTransactionProcessingResult> result = Lists.newArrayList();
        for (Map.Entry<Integer, List<DNSLogEntry>> transaction : transactions.entrySet()) {
            DNSTransactionProcessingResult transactionResult;
            try {
                transactionResult = buildTransactionFromSingleTransactionLogs(transaction.getValue());
            } catch (TransactionNotFoundException e) {
                // We may have missed the query of this transaction.
                continue;
            }

            result.add(transactionResult);
        }

        return result;
    }

    @AutoValue
    public static abstract class DNSTransactionProcessingResult {

        public abstract DNSLogEntry query();
        public abstract List<DNSLogEntry> responses();

        public static DNSTransactionProcessingResult create(DNSLogEntry query, List<DNSLogEntry> responses) {
            return builder()
                    .query(query)
                    .responses(responses)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_DNS_DNSTransactionProcessingResult.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder query(DNSLogEntry query);

            public abstract Builder responses(List<DNSLogEntry> responses);

            public abstract DNSTransactionProcessingResult build();
        }
    }

    public class TransactionNotFoundException extends Exception { }

}