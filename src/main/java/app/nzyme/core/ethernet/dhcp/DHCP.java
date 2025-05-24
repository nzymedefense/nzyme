package app.nzyme.core.ethernet.dhcp;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.dhcp.db.DHCPTransactionEntry;
import app.nzyme.core.util.TimeRange;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DHCP {

    private final NzymeNode nzyme;

    public DHCP(Ethernet ethernet) {
        this.nzyme = ethernet.getNzyme();
    }

    public long countAllTransactions(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(DISTINCT transaction_id) FROM dhcp_transactions " +
                                "WHERE latest_packet >= :tr_from AND latest_packet <= :tr_to " +
                                "AND tap_uuid IN (<taps>)")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<DHCPTransactionEntry> findAllTransactions(TimeRange timeRange, int limit, int offset, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT transaction_id, ANY_VALUE(transaction_type) AS transaction_type, " +
                                "FLOOR(EXTRACT(EPOCH FROM first_packet)::numeric / 10) AS time_bucket, " +
                                "ANY_VALUE(client_mac) AS client_mac, " +
                                "ANY_VALUE(additional_client_macs) AS additional_client_macs, " +
                                "ANY_VALUE(server_mac) AS server_mac, " +
                                "ANY_VALUE(additional_server_macs) AS additional_server_macs, " +
                                "ANY_VALUE(offered_ip_addresses) AS offered_ip_addresses, " +
                                "ANY_VALUE(requested_ip_address) AS requested_ip_address, " +
                                "ANY_VALUE(options_fingerprint) AS options_fingerprint, " +
                                "ANY_VALUE(additional_options_fingerprints) AS additional_options_fingerprints, " +
                                "ANY_VALUE(timestamps) AS timestamps, MAX(first_packet) AS first_packet, " +
                                "MAX(latest_packet) AS latest_packet, ANY_VALUE(notes) AS notes, " +
                                "BOOL_OR(is_successful) AS is_successful, BOOL_OR(is_complete) AS is_complete " +
                                "FROM dhcp_transactions " +
                                "WHERE latest_packet >= :tr_from AND latest_packet <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "GROUP BY transaction_id, time_bucket " +
                                "ORDER BY latest_packet DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
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
                                "ANY_VALUE(options_fingerprint) AS options_fingerprint, " +
                                "ANY_VALUE(additional_options_fingerprints) AS additional_options_fingerprints, " +
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
