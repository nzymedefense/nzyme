package app.nzyme.core.ethernet.l4.tcp;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.l4.tcp.db.TcpSessionEntry;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TCP {

    private final NzymeNode nzyme;

    public TCP(Ethernet ethernet) {
        this.nzyme = ethernet.getNzyme();
    }

    public Optional<TcpSessionEntry> findSessionBySessionKey(String sessionKey,
                                                             DateTime sessionStartTime,
                                                             List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
            handle.createQuery("SELECT * FROM l4_sessions WHERE session_key = :session_key " +
                            "AND start_time >= :tr_from AND start_time <= :tr_to AND tap_uuid IN (<taps>) " +
                            "ORDER BY most_recent_segment_time DESC LIMIT 1")
                    .bind("session_key", sessionKey)
                    .bind("tr_from", sessionStartTime.minusMinutes(1))
                    .bind("tr_to", sessionStartTime.plusMinutes(1))
                    .bindList("taps", taps)
                    .mapTo(TcpSessionEntry.class)
                    .findOne() // In case of the sane session recorded by multiple taps, we take the most recent one.
        );
    }

}
