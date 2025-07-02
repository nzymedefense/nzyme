package app.nzyme.core.ethernet;

import app.nzyme.core.NzymeNodeImpl;
import app.nzyme.core.ethernet.l4.tcp.TcpSessionState;
import app.nzyme.core.ethernet.l4.udp.UdpConversationState;
import app.nzyme.core.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;

public class L4ConnectionCleaner extends Periodical {

    /*
     * This functionality takes care of situations where a tap shuts down with connections still open. Such a tap
     * would never see the FIN/RST in TCP or timeout in UDP, and we'd end up with a connection that exists for forever.
     */

    private static final Logger LOG = LogManager.getLogger(L4ConnectionCleaner.class);

    private final NzymeNodeImpl nzyme;

    public L4ConnectionCleaner(NzymeNodeImpl nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        LOG.debug("Cleaning stale L4 connections.");

        int tcpSessionTimeoutSeconds;
        if (nzyme.getConfiguration().protocols().isPresent()
                && nzyme.getConfiguration().protocols().get().tcp().isPresent()
                && nzyme.getConfiguration().protocols().get().tcp().get().sessionTimeoutSeconds().isPresent()) {
            tcpSessionTimeoutSeconds = nzyme.getConfiguration().protocols().get().tcp().get()
                    .sessionTimeoutSeconds().get();
        } else {
            tcpSessionTimeoutSeconds = 43200;
        }

        // TCP.
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE l4_sessions SET state = :new_state " +
                                "WHERE l4_type = 'TCP' AND most_recent_segment_time < :cutoff " +
                                "AND state NOT IN (<states>)")
                        .bind("new_state", TcpSessionState.CLOSEDTIMEOUTNODE)
                        .bind("cutoff", DateTime.now().minusSeconds(tcpSessionTimeoutSeconds))
                        .bindList("states", List.of(
                                TcpSessionState.CLOSEDFIN,
                                TcpSessionState.CLOSEDRST,
                                TcpSessionState.CLOSEDTIMEOUT,
                                TcpSessionState.CLOSEDTIMEOUTNODE,
                                TcpSessionState.REFUSED)
                        )
                        .execute()
        );

        // UDP.
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE l4_sessions SET state = :new_state " +
                                "WHERE l4_type = 'UDP' AND most_recent_segment_time < :cutoff " +
                                "AND state <> 'CLOSED'")
                        .bind("new_state", UdpConversationState.CLOSEDNODE)
                        .bind("cutoff", DateTime.now().minusSeconds(120))
                        .execute()
        );
    }

    @Override
    public String getName() {
        return "L4ConnectionCleaner";
    }

}
