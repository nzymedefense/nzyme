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

    public long countAllSocksTunnels(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM socks_tunnels " +
                                "WHERE most_recent_segment_time >= :tr_from AND most_recent_segment_time <= :tr_to " +
                                "AND tap_uuid IN (<taps>)")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<SocksTunnelEntry> findAllSocksTunnels(TimeRange timeRange, int limit, int offset, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM socks_tunnels " +
                                "WHERE most_recent_segment_time >= :tr_from AND most_recent_segment_time <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
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
