package app.nzyme.core.periodicals.distributed;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodeUpdater extends Periodical {

    private static final Logger LOG = LogManager.getLogger(NodeUpdater.class);

    private final NzymeNode nzyme;

    public NodeUpdater(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        LOG.debug("Registering node.");

        nzyme.getNodeManager().registerSelf();
    }

    @Override
    public String getName() {
        return "Node Updater";
    }

}
