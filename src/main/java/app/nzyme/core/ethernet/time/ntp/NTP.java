package app.nzyme.core.ethernet.time.ntp;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.database.generic.StringDoubleDoubleNumberAggregationResult;
import app.nzyme.core.database.generic.StringStringNumberAggregationResult;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.time.ntp.db.NTPTransactionEntry;
import app.nzyme.core.shared.db.GenericIntegerHistogramEntry;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.filters.FilterSql;
import app.nzyme.core.util.filters.FilterSqlFragment;
import app.nzyme.core.util.filters.Filters;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NTP {

    private final NzymeNode nzyme;

    public enum OrderColumn {

        INITIATED_AT("COALESCE(ANY_VALUE(timestamp_client_tap_receive), ANY_VALUE(timestamp_server_tap_receive))"),
        TRANSACTION_KEY("ANY_VALUE(transaction_key)"),
        COMPLETE("ANY_VALUE(complete)"),
        CLIENT_ADDRESS("ANY_VALUE(client_address)"),
        CLIENT_MAC("ANY_VALUE(client_mac)"),
        SERVER_ADDRESS("ANY_VALUE(server_address)"),
        SERVER_MAC("ANY_VALUE(server_mac)"),
        STRATUM("ANY_VALUE(stratum)"),
        REFERENCE_ID("ANY_VALUE(reference_id)"),
        RTT_SECONDS("ANY_VALUE(rtt_seconds)"),
        SERVER_PROCESSING_SECONDS("ANY_VALUE(server_processing_seconds)"),
        ROOT_DELAY_SECONDS("ANY_VALUE(root_delay_seconds)"),
        ROOT_DISPERSION_SECONDS("ANY_VALUE(root_dispersion_seconds)");

        private final String columnName;

        OrderColumn(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

    }

    public NTP(Ethernet ethernet) {
        this.nzyme = ethernet.getNzyme();
    }

    public long countAllTransactions(TimeRange timeRange, Filters filters, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new NTPFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(DISTINCT transaction_key) FROM ntp_transactions " +
                                "WHERE (timestamp_client_tap_receive >= :tr_from OR " +
                                "timestamp_server_tap_receive >= :tr_from) AND (timestamp_client_tap_receive <= :tr_to OR " +
                                "timestamp_server_tap_receive <= :tr_to) " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql() +
                                " HAVING 1=1 " + filterFragment.havingSql())
                        .bindList("taps", taps)
                        .bindMap(filterFragment.bindings())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<NTPTransactionEntry> findAllTransactions(TimeRange timeRange,
                                                         Filters filters,
                                                         OrderColumn orderColumn,
                                                         OrderDirection orderDirection,
                                                         int limit,
                                                         int offset,
                                                         List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new NTPFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT transaction_key, BOOL_OR(complete) AS complete, " +
                                "JSONB_AGG(DISTINCT notes) notes, " +
                                "ANY_VALUE(client_mac) AS client_mac, " +
                                "ANY_VALUE(server_mac) AS server_mac, " +
                                "ANY_VALUE(client_address) AS client_address, " +
                                "ANY_VALUE(server_address) AS server_address, " +
                                "ANY_VALUE(client_port) AS client_port, " +
                                "ANY_VALUE(server_port) AS server_port, " +
                                "ANY_VALUE(request_size) AS request_size, " +
                                "ANY_VALUE(response_size) AS response_size, " +
                                "ANY_VALUE(timestamp_client_transmit) AS timestamp_client_transmit, " +
                                "ANY_VALUE(timestamp_server_receive) AS timestamp_server_receive, " +
                                "ANY_VALUE(timestamp_server_transmit) AS timestamp_server_transmit, " +
                                "MIN(timestamp_client_tap_receive) AS timestamp_client_tap_receive, " +
                                "MIN(timestamp_server_tap_receive) AS timestamp_server_tap_receive, " +
                                "ANY_VALUE(server_version) AS server_version, " +
                                "ANY_VALUE(client_version) AS client_version, " +
                                "ANY_VALUE(server_mode) AS server_mode, " +
                                "ANY_VALUE(client_mode) AS client_mode, " +
                                "ANY_VALUE(stratum) AS stratum, " +
                                "ANY_VALUE(leap_indicator) AS leap_indicator, " +
                                "ANY_VALUE(precision) AS precision, " +
                                "ANY_VALUE(poll_interval) AS poll_interval, " +
                                "ANY_VALUE(root_delay_seconds) AS root_delay_seconds, " +
                                "ANY_VALUE(root_dispersion_seconds) AS root_dispersion_seconds, " +
                                "ANY_VALUE(delay_seconds) AS delay_seconds, " +
                                "ANY_VALUE(offset_seconds) AS offset_seconds, " +
                                "ANY_VALUE(rtt_seconds) AS rtt_seconds, " +
                                "ANY_VALUE(server_processing_seconds) AS server_processing_seconds, " +
                                "ANY_VALUE(created_at) AS created_at, " +
                                "ANY_VALUE(reference_id) AS reference_id, " +
                                "ANY_VALUE(transaction_key) AS transaction_key " +
                                "FROM ntp_transactions WHERE (timestamp_client_tap_receive >= :tr_from OR " +
                                "timestamp_server_tap_receive >= :tr_from) AND (timestamp_client_tap_receive <= :tr_to OR " +
                                "timestamp_server_tap_receive <= :tr_to) " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql() +
                                "GROUP BY transaction_key HAVING 1=1 " + filterFragment.havingSql() + " " +
                                "ORDER BY <order_column> <order_direction> " +
                                "LIMIT :limit OFFSET :offset")
                        .bindList("taps", taps)
                        .bindMap(filterFragment.bindings())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .define("order_column", orderColumn.getColumnName())
                        .define("order_direction", orderDirection)
                        .mapTo(NTPTransactionEntry.class)
                        .list()
        );
    }

    public Optional<NTPTransactionEntry> findTransaction(String transactionKey, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Optional.empty();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT transaction_key, BOOL_OR(complete) AS complete, " +
                                "JSONB_AGG(DISTINCT notes) notes, " +
                                "ANY_VALUE(client_mac) AS client_mac, " +
                                "ANY_VALUE(server_mac) AS server_mac, " +
                                "ANY_VALUE(client_address) AS client_address, " +
                                "ANY_VALUE(server_address) AS server_address, " +
                                "ANY_VALUE(client_port) AS client_port, " +
                                "ANY_VALUE(server_port) AS server_port, " +
                                "ANY_VALUE(request_size) AS request_size, " +
                                "ANY_VALUE(response_size) AS response_size, " +
                                "ANY_VALUE(timestamp_client_transmit) AS timestamp_client_transmit, " +
                                "ANY_VALUE(timestamp_server_receive) AS timestamp_server_receive, " +
                                "ANY_VALUE(timestamp_server_transmit) AS timestamp_server_transmit, " +
                                "MIN(timestamp_client_tap_receive) AS timestamp_client_tap_receive, " +
                                "MIN(timestamp_server_tap_receive) AS timestamp_server_tap_receive, " +
                                "ANY_VALUE(server_version) AS server_version, " +
                                "ANY_VALUE(client_version) AS client_version, " +
                                "ANY_VALUE(server_mode) AS server_mode, " +
                                "ANY_VALUE(client_mode) AS client_mode, " +
                                "ANY_VALUE(stratum) AS stratum, " +
                                "ANY_VALUE(leap_indicator) AS leap_indicator, " +
                                "ANY_VALUE(precision) AS precision, " +
                                "ANY_VALUE(poll_interval) AS poll_interval, " +
                                "ANY_VALUE(root_delay_seconds) AS root_delay_seconds, " +
                                "ANY_VALUE(root_dispersion_seconds) AS root_dispersion_seconds, " +
                                "ANY_VALUE(delay_seconds) AS delay_seconds, " +
                                "ANY_VALUE(offset_seconds) AS offset_seconds, " +
                                "ANY_VALUE(rtt_seconds) AS rtt_seconds, " +
                                "ANY_VALUE(server_processing_seconds) AS server_processing_seconds, " +
                                "ANY_VALUE(created_at) AS created_at, " +
                                "ANY_VALUE(reference_id) AS reference_id, " +
                                "ANY_VALUE(transaction_key) AS transaction_key " +
                                "FROM ntp_transactions WHERE transaction_key = :transaction_key " +
                                "AND tap_uuid IN (<taps>) GROUP BY transaction_key LIMIT 1")
                        .bind("transaction_key", transactionKey)
                        .bindList("taps", taps)
                        .mapTo(NTPTransactionEntry.class)
                        .findOne()
        );
    }

    public List<GenericIntegerHistogramEntry> getTransactionCountHistogram(TimeRange timeRange,
                                                                           Bucketing.BucketingConfiguration bucketing,
                                                                           Filters filters,
                                                                           List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new NTPFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, COALESCE(timestamp_client_tap_receive, " +
                                "timestamp_server_tap_receive)) bucket, COUNT(*) AS value " +
                                "FROM ntp_transactions WHERE (timestamp_client_tap_receive >= :tr_from OR " +
                                "timestamp_server_tap_receive >= :tr_from) AND (timestamp_client_tap_receive <= :tr_to " +
                                "OR timestamp_server_tap_receive <= :tr_to) " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql() +
                                "GROUP BY bucket HAVING 1=1 " + filterFragment.havingSql() + " " +
                                "ORDER BY bucket DESC;")
                        .bindList("taps", taps)
                        .bindMap(filterFragment.bindings())
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(GenericIntegerHistogramEntry.class)
                        .list()
        );

    }

    public long countClientRequestResponseRatioHistogramClients(TimeRange timeRange,
                                                                                                     Filters filters,
                                                                                                     List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new NTPFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM (SELECT 1 " +
                                "FROM ntp_transactions WHERE (timestamp_client_tap_receive >= :tr_from OR " +
                                "timestamp_server_tap_receive >= :tr_from) AND " +
                                "(timestamp_client_tap_receive <= :tr_to OR timestamp_server_tap_receive <= :tr_to) " +
                                "AND client_mac IS NOT NULL AND tap_uuid IN (<taps>) " + filterFragment.whereSql() +
                                "GROUP BY client_mac HAVING 1=1 " + filterFragment.havingSql() + ")")
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindMap(filterFragment.bindings())
                        .bindList("taps", taps)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<StringDoubleDoubleNumberAggregationResult> getClientRequestResponseRatioHistogram(TimeRange timeRange,
                                                                                                  Filters filters,
                                                                                                  int limit,
                                                                                                  int offset,
                                                                                                  List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new NTPFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT client_mac AS key, COUNT(*) FILTER " +
                                "(WHERE complete = true)::float / COUNT(*) AS value1, COUNT(*) as value2 " +
                                "FROM ntp_transactions WHERE (timestamp_client_tap_receive >= :tr_from OR " +
                                "timestamp_server_tap_receive >= :tr_from) AND " +
                                "(timestamp_client_tap_receive <= :tr_to OR timestamp_server_tap_receive <= :tr_to) " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql() +
                                "GROUP BY client_mac HAVING 1=1 " + filterFragment.havingSql() +
                                "ORDER BY value1 ASC LIMIT :limit OFFSET :offset ")
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .bindMap(filterFragment.bindings())
                        .bindList("taps", taps)
                        .mapTo(StringDoubleDoubleNumberAggregationResult.class)
                        .list()
        );
    }

    public long countTopServersHistogramServers(TimeRange timeRange,
                                                Filters filters,
                                                List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new NTPFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM (SELECT 1 FROM ntp_transactions WHERE " +
                                "(timestamp_client_tap_receive >= :tr_from OR timestamp_server_tap_receive >= " +
                                ":tr_from) AND (timestamp_client_tap_receive <= :tr_to OR " +
                                "timestamp_server_tap_receive <= :tr_to) " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql() +
                                "GROUP BY server_mac, server_address HAVING 1=1 " + filterFragment.havingSql() + ")")
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindMap(filterFragment.bindings())
                        .bindList("taps", taps)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<StringStringNumberAggregationResult> getTopServersHistogram(TimeRange timeRange,
                                                                            Filters filters,
                                                                            int limit,
                                                                            int offset,
                                                                            List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new NTPFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT server_address AS key, server_mac AS value1, COUNT(*) AS value2 " +
                                "FROM ntp_transactions WHERE (timestamp_client_tap_receive >= :tr_from OR " +
                                "timestamp_server_tap_receive >= :tr_from) AND " +
                                "(timestamp_client_tap_receive <= :tr_to OR " +
                                "timestamp_server_tap_receive <= :tr_to) " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql() +
                                "GROUP BY server_mac, server_address HAVING 1=1 " + filterFragment.havingSql() +
                                "ORDER BY value2 DESC LIMIT :limit OFFSET :offset")
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .bindMap(filterFragment.bindings())
                        .bindList("taps", taps)
                        .mapTo(StringStringNumberAggregationResult.class)
                        .list()
        );
    }

}
