package app.nzyme.core.ethernet;

import app.nzyme.core.NzymeNodeImpl;
import app.nzyme.core.ethernet.tcp.TcpSessionState;
import app.nzyme.core.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;

public class EthernetConnectionCleaner extends Periodical {

    /*
     * This functionality takes care of situations where a tap shuts down with connections still open. Such a tap
     * would never see the FIN/RST, and we'd end up with a connection that exists for forever.
     */

    private static final Logger LOG = LogManager.getLogger(EthernetConnectionCleaner.class);

    private final NzymeNodeImpl nzyme;

    public EthernetConnectionCleaner(NzymeNodeImpl nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        LOG.debug("Cleaning stale Ethernet connections.");

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
    }

    @Override
    public String getName() {
        return "EthernetConnectionCleaner";
    }

}
