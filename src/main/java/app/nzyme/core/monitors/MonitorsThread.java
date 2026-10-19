package app.nzyme.core.monitors;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.monitors.db.MonitorEntry;
import app.nzyme.core.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

public class MonitorsThread extends Periodical {

    private static final Logger LOG = LogManager.getLogger(MonitorsThread.class);

    /*
     * A monitor that has been PENDING or EXECUTING for longer than this (or three times its interval, whichever
     * is longer) is considered stuck. This happens when a node restarts or a task is lost while the monitor is
     * in flight, because only the executing node sets the monitor back to IDLE.
     */
    public static final Duration MINIMUM_STUCK_TIMEOUT = Duration.standardMinutes(5);

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

                if (isStuck(monitor, status)) {
                    LOG.warn("Monitor [{}] has been in status [{}] since [{}]. Considering it stuck and " +
                            "resetting it to IDLE.", monitor.uuid(), status, monitor.statusChangedAt());
                    nzyme.getMonitors().setMonitorStatus(monitor.uuid(), MonitorStatus.IDLE);
                    status = MonitorStatus.IDLE;
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

    public static boolean isStuck(MonitorEntry monitor, MonitorStatus status) {
        if (status != MonitorStatus.PENDING && status != MonitorStatus.EXECUTING) {
            return false;
        }

        // Monitors that changed status before the status timestamp existed are stuck by definition.
        if (monitor.statusChangedAt() == null) {
            return true;
        }

        Duration intervalBased = Duration.standardMinutes(monitor.interval()).multipliedBy(3);
        Duration timeout = intervalBased.isLongerThan(MINIMUM_STUCK_TIMEOUT) ? intervalBased : MINIMUM_STUCK_TIMEOUT;

        return monitor.statusChangedAt().plus(timeout).isBefore(DateTime.now());
    }

    @Override
    public String getName() {
        return "MonitorsThread";
    }
}
