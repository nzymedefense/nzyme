package app.nzyme.core.monitors;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.monitors.db.MonitorEntry;
import app.nzyme.core.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class MonitorsThread extends Periodical {

    private static final Logger LOG = LogManager.getLogger(MonitorsThread.class);

    private final NzymeNode nzyme;

    /*
     * This thread runs on all nodes and checks if any monitors are, based on their configured interval, due
     * to execute. It immediately sets them to pending, so other nodes don't schedule the same monitor.
     */

    public MonitorsThread(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        for (MonitorEntry monitor : nzyme.getMonitors().findAllMonitorsOfAllTenants()) {
            try {
                MonitorStatus status;
                try {
                    status = MonitorStatus.valueOf(monitor.status());
                } catch (IllegalArgumentException e) {
                    LOG.error("Unknown monitor status [{}]. Skipping.", monitor.status());
                    continue;
                }

                boolean executionDue;
                if (monitor.lastRun() == null) {
                    executionDue = true;
                } else {
                    executionDue = !DateTime.now().isBefore(monitor.lastRun()
                                    .plusMinutes(monitor.interval())
                                    .minusSeconds(30));
                }

                if (executionDue && status != MonitorStatus.PENDING && status != MonitorStatus.EXECUTING) {
                    nzyme.getMonitors().setMonitorStatus(monitor.uuid(), MonitorStatus.PENDING);
                    
                    nzyme.getTasksQueue().publish(new MonitorExecutionTask(monitor));
                    LOG.debug("Submitted execution task for due monitor [{}].", monitor.uuid());
                } else {
                    LOG.debug("Monitor [{}] not due for execution: [{}/{}/{}]",
                            monitor.uuid(), monitor.interval(), monitor.lastRun(), monitor.status());
                }
            } catch (Exception e) {
                LOG.error("Could not run monitor [{}] execution check. Skipping.", monitor.uuid(), e);
            }
        }
    }

    @Override
    public String getName() {
        return "MonitorsThread";
    }
}
