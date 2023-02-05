package app.nzyme.core.monitoring.health;

import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Indicator {

    private static final Logger LOG = LogManager.getLogger(Indicator.class);

    public IndicatorStatus run() {
        LOG.debug("Running health check indicator [{}].", getName());
        IndicatorStatus result = doRun();
        LOG.debug("Finished health check indicator run of [{}].", getName());

        return result;
    }

    protected abstract IndicatorStatus doRun();

    public abstract String getId();
    public abstract String getName();

}
