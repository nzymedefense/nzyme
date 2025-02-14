package app.nzyme.core.uav;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.uav.db.UavEntry;
import app.nzyme.core.util.TimeRange;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Uav {

    private final NzymeNode nzyme;

    public Uav(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public long countAllUavs(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM uavs " +
                                "WHERE last_seen >= :tr_from AND last_seen <= :tr_to " +
                                "AND tap_uuid IN (<taps>)")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<UavEntry> findAllUavs(TimeRange timeRange, int limit, int offset, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM uavs " +
                                "WHERE last_seen >= :tr_from AND last_seen <= :tr_to AND tap_uuid IN (<taps>) " +
                                "ORDER BY last_seen DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(UavEntry.class)
                        .list()
        );
    }

}
