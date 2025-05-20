package app.nzyme.core.ethernet.socks;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.socks.db.SocksTunnelEntry;
import app.nzyme.core.util.TimeRange;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Socks {

    private final NzymeNode nzyme;

    public Socks(Ethernet ethernet) {
        this.nzyme = ethernet.getNzyme();
    }

    public long countAllTunnels(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(DISTINCT tcp_session_key) FROM socks_tunnels " +
                                "WHERE most_recent_segment_time >= :tr_from AND most_recent_segment_time <= :tr_to " +
                                "AND tap_uuid IN (<taps>)")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<SocksTunnelEntry> findAllTunnels(TimeRange timeRange, int limit, int offset, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT tcp_session_key, " +
                                "ANY_VALUE(socks_type) AS socks_type, " +
                                "ANY_VALUE(authentication_status) AS authentication_status, " +
                                "ANY_VALUE(handshake_status) AS handshake_status, A" +
                                "NY_VALUE(connection_status) AS connection_status, " +
                                "ANY_VALUE(username) AS username, " +
                                "MAX(tunneled_bytes) AS tunneled_bytes, " +
                                "ANY_VALUE(tunneled_destination_address) AS tunneled_destination_address, " +
                                "ANY_VALUE(tunneled_destination_host) AS tunneled_destination_host, " +
                                "ANY_VALUE(tunneled_destination_port) AS tunneled_destination_port, " +
                                "MIN(established_at) AS established_at, " +
                                "MAX(terminated_at) AS terminated_at, " +
                                "MAX(most_recent_segment_time) AS most_recent_segment_time " +
                                "FROM socks_tunnels " +
                                "WHERE most_recent_segment_time >= :tr_from AND most_recent_segment_time <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "GROUP BY tcp_session_key " +
                                "ORDER BY most_recent_segment_time DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(SocksTunnelEntry.class)
                        .list()
        );
    }

}
