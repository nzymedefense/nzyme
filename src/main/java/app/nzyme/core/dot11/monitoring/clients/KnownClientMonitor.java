package app.nzyme.core.dot11.monitoring.clients;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KnownClientMonitor extends Periodical {

    private static final Logger LOG = LogManager.getLogger(KnownClientMonitor.class);

    private final NzymeNode nzyme;

    public KnownClientMonitor(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        try {

        } catch (Exception e) {
            LOG.error("Could not run client monitoring.", e);
        }
    }

    @Override
    public String getName() {
        return "KnownClientMonitor";
    }

}
