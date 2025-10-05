package app.nzyme.core.ethernet.dhcp;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.dhcp.db.DHCPTransactionEntry;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.filters.FilterSql;
import app.nzyme.core.util.filters.FilterSqlFragment;
import app.nzyme.core.util.filters.Filters;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DHCP {

    public enum OrderColumn {

        INITIATED_AT("first_packet"),
        TRANSACTION_TYPE("transaction_type"),
        CLIENT_MAC("client_mac"),
        SERVER_MAC("server_mac"),
        REQUESTED_IP_ADDRESS("requested_ip_address"),
        FINGERPRINT("fingerprint");

        private final String columnName;

        OrderColumn(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

    }

    private final NzymeNode nzyme;

    public DHCP(Ethernet ethernet) {
        this.nzyme = ethernet.getNzyme();
    }

    public long countAllTransactions(TimeRange timeRange, Filters filters, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new DHCPFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(DISTINCT transaction_id) FROM dhcp_transactions " +
                                "WHERE latest_packet >= :tr_from AND latest_packet <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql())
                        .bindList("taps", taps)
                        .bindMap(filterFragment.bindings())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<DHCPTransactionEntry> findAllTransactions(TimeRange timeRange,
                                                          int limit,
                                                          int offset,
                                                          OrderColumn orderColumn,
                                                          OrderDirection orderDirection,
                                                          Filters filters,
                                                          List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new DHCPFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT transaction_id, ANY_VALUE(transaction_type) AS transaction_type, " +
                                "FLOOR(EXTRACT(EPOCH FROM first_packet)::numeric / 10) AS time_bucket, " +
                                "ANY_VALUE(client_mac) AS client_mac, " +
                                "ANY_VALUE(additional_client_macs) AS additional_client_macs, " +
                                "ANY_VALUE(server_mac) AS server_mac, " +
                                "ANY_VALUE(additional_server_macs) AS additional_server_macs, " +
                                "ANY_VALUE(offered_ip_addresses) AS offered_ip_addresses, " +
                                "ANY_VALUE(requested_ip_address) AS requested_ip_address, " +
                                "ANY_VALUE(options) AS options, ANY_VALUE(additional_options) AS additional_options, " +
                                "ANY_VALUE(fingerprint) AS fingerprint, " +
                                "ANY_VALUE(additional_fingerprints) AS additional_fingerprints, " +
                                "ANY_VALUE(vendor_class) AS vendor_class, " +
                                "ANY_VALUE(additional_vendor_classes) AS additional_vendor_classes, " +
                                "ANY_VALUE(timestamps) AS timestamps, MAX(first_packet) AS first_packet, " +
                                "MAX(latest_packet) AS latest_packet, ANY_VALUE(notes) AS notes, " +
                                "BOOL_OR(is_successful) AS is_successful, BOOL_OR(is_complete) AS is_complete " +
                                "FROM dhcp_transactions " +
                                "WHERE latest_packet >= :tr_from AND latest_packet <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql() + " " +
                                "GROUP BY transaction_id, time_bucket " +
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
                        .mapTo(DHCPTransactionEntry.class)
                        .list()
        );
    }

    public Optional<DHCPTransactionEntry> findTransaction(long transactionId, DateTime transactionTime, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Optional.empty();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT transaction_id, ANY_VALUE(transaction_type) AS transaction_type, " +
                                "ANY_VALUE(client_mac) AS client_mac, " +
                                "ANY_VALUE(additional_client_macs) AS additional_client_macs, " +
                                "ANY_VALUE(server_mac) AS server_mac, " +
                                "ANY_VALUE(additional_server_macs) AS additional_server_macs, " +
                                "ANY_VALUE(offered_ip_addresses) AS offered_ip_addresses, " +
                                "ANY_VALUE(requested_ip_address) AS requested_ip_address, " +
                                "ANY_VALUE(options) AS options, ANY_VALUE(additional_options) AS additional_options, " +
                                "ANY_VALUE(fingerprint) AS fingerprint, " +
                                "ANY_VALUE(additional_fingerprints) AS additional_fingerprints, " +
                                "ANY_VALUE(vendor_class) AS vendor_class, " +
                                "ANY_VALUE(additional_vendor_classes) AS additional_vendor_classes, " +
                                "ANY_VALUE(timestamps) AS timestamps, MAX(first_packet) AS first_packet, " +
                                "MAX(latest_packet) AS latest_packet, ANY_VALUE(notes) AS notes, " +
                                "BOOL_OR(is_successful) AS is_successful, BOOL_OR(is_complete) AS is_complete " +
                                "FROM dhcp_transactions " +
                                "WHERE transaction_id = :transaction_id " +
                                "AND (first_packet BETWEEN (:transaction_time::timestamptz - INTERVAL '10 seconds') " +
                                "AND (:transaction_time::timestamptz + INTERVAL '10 seconds'))" +
                                "AND tap_uuid IN (<taps>) " +
                                "GROUP BY transaction_id " +
                                "ORDER BY latest_packet DESC LIMIT 1")
                        .bind("transaction_id", transactionId)
                        .bind("transaction_time", transactionTime)
                        .bindList("taps", taps)
                        .mapTo(DHCPTransactionEntry.class)
                        .findOne()
        );
    }

}
