package app.nzyme.core.ethernet.socks;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.socks.db.SocksTunnelEntry;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.filters.FilterSql;
import app.nzyme.core.util.filters.FilterSqlFragment;
import app.nzyme.core.util.filters.Filters;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SOCKS {

    private final NzymeNode nzyme;

    public enum OrderColumn {

        TUNNEL_ID("ANY_VALUE(socks.uuid)"),
        CLIENT_ADDRESS("ANY_VALUE(tcp.source_address)"),
        CLIENT_MAC("ANY_VALUE(tcp.source_mac)"),
        SERVER_ADDRESS("ANY_VALUE(tcp.destination_address)"),
        SERVER_MAC("ANY_VALUE(tcp.destination_mac)"),
        TYPE("socks_type"),
        CONNECTION_STATUS("connection_status"),
        TUNNELED_BYTES("tunneled_bytes"),
        ESTABLISHED_AT("established_at"),
        TERMINATED_AT("terminated_at"),
        DURATION("duration_ms");

        private final String columnName;

        OrderColumn(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

    }

    public SOCKS(Ethernet ethernet) {
        this.nzyme = ethernet.getNzyme();
    }

    public long countAllTunnels(TimeRange timeRange, Filters filters, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new SOCKSFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM (SELECT socks.tcp_session_key, " +
                                "ANY_VALUE(socks.socks_type) AS socks_type, " +
                                "ANY_VALUE(socks.authentication_status) AS authentication_status, " +
                                "ANY_VALUE(socks.handshake_status) AS handshake_status, " +
                                "ANY_VALUE(socks.connection_status) AS connection_status, " +
                                "ANY_VALUE(socks.username) AS username, " +
                                "MAX(socks.tunneled_bytes) AS tunneled_bytes, " +
                                "ANY_VALUE(socks.tunneled_destination_address) AS tunneled_destination_address, " +
                                "ANY_VALUE(socks.tunneled_destination_host) AS tunneled_destination_host, " +
                                "ANY_VALUE(socks.tunneled_destination_port) AS tunneled_destination_port, " +
                                "MIN(socks.established_at) AS established_at, " +
                                "MAX(socks.terminated_at) AS terminated_at, " +
                                "MAX(socks.most_recent_segment_time) AS most_recent_segment_time " +
                                "FROM socks_tunnels AS socks " +
                                "LEFT JOIN l4_sessions AS tcp ON tcp.session_key = socks.tcp_session_key " +
                                "AND tcp.l4_type = 'TCP' AND tcp.tap_uuid IN (<taps>) " +
                                "AND tcp.start_time >= (socks.established_at - interval '1 minute') " +
                                "AND tcp.start_time <= (socks.established_at + interval '1 minute') " +
                                "WHERE socks.most_recent_segment_time >= :tr_from " +
                                "AND socks.most_recent_segment_time <= :tr_to " +
                                "AND socks.tap_uuid IN (<taps>) " + filterFragment.whereSql() +
                                "GROUP BY tcp_session_key HAVING 1=1 " +  filterFragment.havingSql() +
                                ") AS ignored")
                        .bindList("taps", taps)
                        .bindMap(filterFragment.bindings())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<SocksTunnelEntry> findAllTunnels(TimeRange timeRange,
                                                 Filters filters,
                                                 OrderColumn orderColumn,
                                                 OrderDirection orderDirection,
                                                 int limit,
                                                 int offset,
                                                 List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new SOCKSFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT socks.tcp_session_key, " +
                                "ANY_VALUE(socks.socks_type) AS socks_type, " +
                                "ANY_VALUE(socks.authentication_status) AS authentication_status, " +
                                "ANY_VALUE(socks.handshake_status) AS handshake_status, " +
                                "ANY_VALUE(socks.connection_status) AS connection_status, " +
                                "ANY_VALUE(socks.username) AS username, " +
                                "MAX(socks.tunneled_bytes) AS tunneled_bytes, " +
                                "ANY_VALUE(socks.tunneled_destination_address) AS tunneled_destination_address, " +
                                "ANY_VALUE(socks.tunneled_destination_host) AS tunneled_destination_host, " +
                                "ANY_VALUE(socks.tunneled_destination_port) AS tunneled_destination_port, " +
                                "MIN(socks.established_at) AS established_at, " +
                                "MAX(socks.terminated_at) AS terminated_at, " +
                                "MAX(socks.most_recent_segment_time) AS most_recent_segment_time, " +
                                "(EXTRACT(EPOCH FROM " +
                                "(MAX(socks.most_recent_segment_time) - MIN(socks.established_at))) * 1000" +
                                ") AS duration_ms " +
                                "FROM socks_tunnels AS socks " +
                                "LEFT JOIN l4_sessions AS tcp ON tcp.session_key = socks.tcp_session_key " +
                                "AND tcp.l4_type = 'TCP' AND tcp.tap_uuid = socks.tap_uuid " +
                                "AND tcp.start_time >= (socks.established_at - interval '1 minute') " +
                                "AND tcp.start_time <= (socks.established_at + interval '1 minute') " +
                                "WHERE socks.most_recent_segment_time >= :tr_from " +
                                "AND socks.most_recent_segment_time <= :tr_to " +
                                "AND socks.tap_uuid IN (<taps>) " + filterFragment.whereSql() +
                                "GROUP BY tcp_session_key HAVING 1=1 " +  filterFragment.havingSql() +
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
                        .mapTo(SocksTunnelEntry.class)
                        .list()
        );
    }

    public Optional<SocksTunnelEntry> findTunnel(String sessionKey, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Optional.empty();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT socks.tcp_session_key, " +
                                "ANY_VALUE(socks.socks_type) AS socks_type, " +
                                "ANY_VALUE(socks.authentication_status) AS authentication_status, " +
                                "ANY_VALUE(socks.handshake_status) AS handshake_status, " +
                                "ANY_VALUE(socks.connection_status) AS connection_status, " +
                                "ANY_VALUE(socks.username) AS username, " +
                                "MAX(socks.tunneled_bytes) AS tunneled_bytes, " +
                                "ANY_VALUE(socks.tunneled_destination_address) AS tunneled_destination_address, " +
                                "ANY_VALUE(socks.tunneled_destination_host) AS tunneled_destination_host, " +
                                "ANY_VALUE(socks.tunneled_destination_port) AS tunneled_destination_port, " +
                                "MIN(socks.established_at) AS established_at, " +
                                "MAX(socks.terminated_at) AS terminated_at, " +
                                "MAX(socks.most_recent_segment_time) AS most_recent_segment_time, " +
                                "(EXTRACT(EPOCH FROM " +
                                "(MAX(socks.most_recent_segment_time) - MIN(socks.established_at))) * 1000" +
                                ") AS duration_ms " +
                                "FROM socks_tunnels AS socks " +
                                "LEFT JOIN l4_sessions AS tcp ON tcp.session_key = socks.tcp_session_key " +
                                "AND tcp.l4_type = 'TCP' AND tcp.tap_uuid = socks.tap_uuid " +
                                "WHERE socks.tcp_session_key = :tcp_session_key AND socks.tap_uuid IN (<taps>) " +
                                "GROUP BY tcp_session_key")
                        .bindList("taps", taps)
                        .bind("tcp_session_key", sessionKey)
                        .mapTo(SocksTunnelEntry.class)
                        .findOne()
        );
    }

}
