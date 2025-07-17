package app.nzyme.core.ethernet.arp;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.arp.db.ArpPacketEntry;
import app.nzyme.core.util.TimeRange;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ARP {

    public enum OrderColumn {

        TIMESTAMP("timestamp"),
        ETHERNET_SOURCE_MAC("ethernet_source_mac"),
        ETHERNET_DESTINATION_MAC("ethernet_destination_mac"),
        HARDWARE_TYPE("hardware_type"),
        PROTOCOL_TYPE("protocol_type"),
        OPERATION("operation"),
        ARP_SENDER_MAC("arp_sender_mac"),
        ARP_SENDER_ADDRESS("arp_sender_address"),
        ARP_TARGET_MAC("arp_target_mac"),
        ARP_TARGET_ADDRESS("arp_target_address");

        private final String columnName;

        OrderColumn(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

    }

    private final NzymeNode nzyme;

    public ARP(Ethernet ethernet) {
        this.nzyme = ethernet.getNzyme();
    }

    public long countAllPackets(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM arp_packets " +
                                "WHERE timestamp >= :tr_from AND timestamp <= :tr_to " +
                                "AND tap_uuid IN (<taps>)")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }


    public List<ArpPacketEntry> findAllPackets(TimeRange timeRange,
                                               int limit,
                                               int offset,
                                               OrderColumn orderColumn,
                                               OrderDirection orderDirection,
                                               List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM arp_packets " +
                                "WHERE timestamp >= :tr_from AND timestamp <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "ORDER BY <order_column> <order_direction> " +
                                "LIMIT :limit OFFSET :offset")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .define("order_column", orderColumn.getColumnName())
                        .define("order_direction", orderDirection)
                        .mapTo(ArpPacketEntry.class)
                        .list()
        );
    }

}
