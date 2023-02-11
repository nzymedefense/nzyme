package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.distributed.Node;
import app.nzyme.core.distributed.NodeManager;
import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import org.joda.time.DateTime;

public class NodeOfflineIndicator extends Indicator {

    private final NodeManager nodeManager;

    public NodeOfflineIndicator(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @Override
    protected IndicatorStatus doRun() {
        for (Node node : nodeManager.getNodes()) {
            if (!node.deleted() && !node.isEphemeral() && node.lastSeen().isBefore(DateTime.now().minusMinutes(2))) {
                return IndicatorStatus.orange(this);
            }
        }

        return IndicatorStatus.green(this);
    }

    @Override
    public String getId() {
        return "node_offline";
    }

    @Override
    public String getName() {
        return "Node Offline";
    }

}
