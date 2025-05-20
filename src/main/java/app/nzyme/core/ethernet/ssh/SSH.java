package app.nzyme.core.ethernet.ssh;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.socks.db.SocksTunnelEntry;
import app.nzyme.core.ethernet.ssh.db.SSHSessionEntry;
import app.nzyme.core.util.TimeRange;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SSH {

    private final NzymeNode nzyme;

    public SSH(Ethernet ethernet) {
        this.nzyme = ethernet.getNzyme();
    }

    public long countAllSessions(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(DISTINCT tcp_session_key) FROM ssh_sessions " +
                                "WHERE most_recent_segment_time >= :tr_from AND most_recent_segment_time <= :tr_to " +
                                "AND tap_uuid IN (<taps>)")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<SSHSessionEntry> findAllSessions(TimeRange timeRange, int limit, int offset, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT tcp_session_key, " +
                                "ANY_VALUE(client_version_version) AS client_version_version, " +
                                "ANY_VALUE(client_version_software) AS client_version_software, " +
                                "ANY_VALUE(client_version_comments) AS client_version_comments, " +
                                "ANY_VALUE(server_version_version) AS server_version_version, " +
                                "ANY_VALUE(server_version_software) AS server_version_software, " +
                                "ANY_VALUE(server_version_comments) AS server_version_comments, " +
                                "ANY_VALUE(connection_status) AS connection_status, " +
                                "MAX(tunneled_bytes) AS tunneled_bytes, " +
                                "MIN(established_at) AS established_at, " +
                                "MAX(terminated_at) AS terminated_at, " +
                                "MAX(most_recent_segment_time) AS most_recent_segment_time " +
                                "FROM ssh_sessions WHERE most_recent_segment_time >= :tr_from AND most_recent_segment_time <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "GROUP BY tcp_session_key " +
                                "ORDER BY most_recent_segment_time DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(SSHSessionEntry.class)
                        .list()
        );
    }

}
