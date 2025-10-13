package app.nzyme.core.ethernet.ssh;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.ssh.db.SSHSessionEntry;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.filters.FilterSql;
import app.nzyme.core.util.filters.FilterSqlFragment;
import app.nzyme.core.util.filters.Filters;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SSH {

    public enum OrderColumn {

        SESSION_ID("ANY_VALUE(ssh.uuid)"),
        CLIENT_ADDRESS("ANY_VALUE(tcp.source_address)"),
        CLIENT_MAC("ANY_VALUE(tcp.source_mac)"),
        CLIENT_TYPE("ANY_VALUE(client_version_software) || ANY_VALUE(client_version_version) || ANY_VALUE(client_version_comments)"),
        SERVER_ADDRESS("ANY_VALUE(tcp.destination_address)"),
        SERVER_MAC("ANY_VALUE(tcp.destination_mac)"),
        SERVER_TYPE("ANY_VALUE(server_version_software) || ANY_VALUE(server_version_version) || ANY_VALUE(server_version_comments)"),
        CONNECTION_STATUS("connection_status"),
        TUNNELED_BYTES("tunneled_bytes"),
        ESTABLISHED_AT("established_at"),
        TERMINATED_AT("terminated_at");

        private final String columnName;

        OrderColumn(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

    }

    private final NzymeNode nzyme;

    public SSH(Ethernet ethernet) {
        this.nzyme = ethernet.getNzyme();
    }

    public long countAllSessions(TimeRange timeRange, Filters filters, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new SSHFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM (SELECT ssh.tcp_session_key, " +
                                "ANY_VALUE(ssh.client_version_version) AS client_version_version, " +
                                "ANY_VALUE(ssh.client_version_software) AS client_version_software, " +
                                "ANY_VALUE(ssh.client_version_comments) AS client_version_comments, " +
                                "ANY_VALUE(ssh.server_version_version) AS server_version_version, " +
                                "ANY_VALUE(ssh.server_version_software) AS server_version_software, " +
                                "ANY_VALUE(ssh.server_version_comments) AS server_version_comments, " +
                                "ANY_VALUE(ssh.connection_status) AS connection_status, " +
                                "MAX(ssh.tunneled_bytes) AS tunneled_bytes, " +
                                "MIN(ssh.established_at) AS established_at, " +
                                "MAX(ssh.terminated_at) AS terminated_at, " +
                                "MAX(ssh.most_recent_segment_time) AS most_recent_segment_time " +
                                "FROM ssh_sessions AS ssh " +
                                "LEFT JOIN l4_sessions AS tcp ON tcp.session_key = ssh.tcp_session_key " +
                                "AND tcp.l4_type = 'TCP' AND tcp.tap_uuid IN (<taps>) " +
                                "WHERE ssh.most_recent_segment_time >= :tr_from AND ssh.most_recent_segment_time <= :tr_to " +
                                "AND ssh.tap_uuid IN (<taps>) " + filterFragment.whereSql() +
                                "GROUP BY tcp_session_key HAVING 1=1 " + filterFragment.havingSql() + ") AS ignored")
                        .bindList("taps", taps)
                        .bindMap(filterFragment.bindings())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<SSHSessionEntry> findAllSessions(TimeRange timeRange,
                                                 Filters filters,
                                                 OrderColumn orderColumn,
                                                 OrderDirection orderDirection,
                                                 int limit,
                                                 int offset,
                                                 List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new SSHFilters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT ssh.tcp_session_key, " +
                                "ANY_VALUE(ssh.client_version_version) AS client_version_version, " +
                                "ANY_VALUE(ssh.client_version_software) AS client_version_software, " +
                                "ANY_VALUE(ssh.client_version_comments) AS client_version_comments, " +
                                "ANY_VALUE(ssh.server_version_version) AS server_version_version, " +
                                "ANY_VALUE(ssh.server_version_software) AS server_version_software, " +
                                "ANY_VALUE(ssh.server_version_comments) AS server_version_comments, " +
                                "ANY_VALUE(ssh.connection_status) AS connection_status, " +
                                "MAX(ssh.tunneled_bytes) AS tunneled_bytes, " +
                                "MIN(ssh.established_at) AS established_at, " +
                                "MAX(ssh.terminated_at) AS terminated_at, " +
                                "MAX(ssh.most_recent_segment_time) AS most_recent_segment_time " +
                                "FROM ssh_sessions AS ssh " +
                                "LEFT JOIN l4_sessions AS tcp ON tcp.session_key = ssh.tcp_session_key " +
                                "AND tcp.l4_type = 'TCP' AND tcp.tap_uuid = ssh.tap_uuid " +
                                "AND tcp.start_time >= (ssh.established_at - interval '1 minute') " +
                                "AND tcp.start_time <= (ssh.established_at + interval '1 minute') " +
                                "WHERE ssh.most_recent_segment_time >= :tr_from " +
                                "AND ssh.most_recent_segment_time <= :tr_to " +
                                "AND ssh.tap_uuid IN (<taps>) " + filterFragment.whereSql() +
                                "GROUP BY tcp_session_key HAVING 1=1 " + filterFragment.havingSql() +
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
                        .mapTo(SSHSessionEntry.class)
                        .list()
        );
    }

    public Optional<SSHSessionEntry> findSession(String sessionKey, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Optional.empty();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT ssh.tcp_session_key, " +
                                "ANY_VALUE(ssh.client_version_version) AS client_version_version, " +
                                "ANY_VALUE(ssh.client_version_software) AS client_version_software, " +
                                "ANY_VALUE(ssh.client_version_comments) AS client_version_comments, " +
                                "ANY_VALUE(ssh.server_version_version) AS server_version_version, " +
                                "ANY_VALUE(ssh.server_version_software) AS server_version_software, " +
                                "ANY_VALUE(ssh.server_version_comments) AS server_version_comments, " +
                                "ANY_VALUE(ssh.connection_status) AS connection_status, " +
                                "MAX(ssh.tunneled_bytes) AS tunneled_bytes, " +
                                "MIN(ssh.established_at) AS established_at, " +
                                "MAX(ssh.terminated_at) AS terminated_at, " +
                                "MAX(ssh.most_recent_segment_time) AS most_recent_segment_time " +
                                "FROM ssh_sessions AS ssh " +
                                "LEFT JOIN l4_sessions AS tcp ON tcp.session_key = ssh.tcp_session_key " +
                                "AND tcp.l4_type = 'TCP' AND tcp.tap_uuid = ssh.tap_uuid " +
                                "AND tcp.start_time >= (ssh.established_at - interval '1 minute') " +
                                "AND tcp.start_time <= (ssh.established_at + interval '1 minute') " +
                                "WHERE ssh.tcp_session_key = :tcp_session_key AND ssh.tap_uuid IN (<taps>) " +
                                "GROUP BY tcp_session_key")
                        .bindList("taps", taps)
                        .bind("tcp_session_key", sessionKey)
                        .mapTo(SSHSessionEntry.class)
                        .findOne()
        );
    }

}
