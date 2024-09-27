package app.nzyme.core.dot11.monitoring.clients;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.ClientWithOrganizationAndTenant;
import app.nzyme.core.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class KnownClientMonitor extends Periodical {

    private static final Logger LOG = LogManager.getLogger(KnownClientMonitor.class);

    private final NzymeNode nzyme;

    public KnownClientMonitor(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        try {
            // Fetch all clients we saw in previous minute.
            List<ClientWithOrganizationAndTenant> clients = nzyme.getDot11()
                    .findAllRecentlyConnectedClientsAndOwner(1);

            nzyme.getDatabase().useHandle(handle -> {
                for (ClientWithOrganizationAndTenant client : clients) {
                    // check if monitored etc
                }
            });
        } catch (Exception e) {
            LOG.error("Could not run client monitoring.", e);
        }
    }

    @Override
    public String getName() {
        return "KnownClientMonitor";
    }

}
