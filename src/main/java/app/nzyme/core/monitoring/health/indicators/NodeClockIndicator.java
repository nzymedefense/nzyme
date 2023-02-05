package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.distributed.Node;
import app.nzyme.core.distributed.NodeManager;
import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class NodeClockIndicator extends Indicator {

    private static final Logger LOG = LogManager.getLogger(NodeClockIndicator.class);

    private final NodeManager nodeManager;

    public NodeClockIndicator(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @Override
    protected IndicatorStatus doRun() {
        for (Node node : nodeManager.getActiveNodes()) {
            // We only want to check very recently active nodes.
            if (node.lastSeen().isBefore(DateTime.now().minusMinutes(2))) {
                LOG.debug("Skipping inactive node [{}/{}].", node.name(), node.uuid());
                continue;
            }

            if (node.clockDriftMs() < -5000 || node.clockDriftMs() > 5000) {
                return IndicatorStatus.red(this);
            }
        }

        return IndicatorStatus.green(this);
    }

    @Override
    public String getId() {
        return "node_clock";
    }

    @Override
    public String getName() {
        return "Node Clock";
    }

}
