package app.nzyme.core.timelines;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.timelines.tasks.Dot11BSSIDTimelineCalculationTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.Optional;

public class TimelinesThread extends Periodical {

    private static final Logger LOG = LogManager.getLogger(TimelinesThread.class);

    private final NzymeNode nzyme;

    /*
     * This thread runs on all nodes and submits a task to run timeline calculations if due. This makes sure that only
     * one node is calculating timelines.
     */

    public TimelinesThread(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        try {
            // 802.11 BSSIDs
            Optional<DateTime> bssidsLastExecution = nzyme.getDatabaseCoreRegistry()
                    .getValue(TimelinesRegistryKeys.TIMELINES_DOT11_BSSIDS_LAST_EXECUTION.key())
                    .map(DateTime::parse);
            if (bssidsLastExecution.isEmpty() || bssidsLastExecution.get().isBefore(DateTime.now().minusMinutes(1).plusSeconds(10))) {
                // Due for execution.
                nzyme.getTasksQueue().publish(new Dot11BSSIDTimelineCalculationTask());
            }

            // 802.11 SSIDs

            // 802.11 Connected Clients

            // 802.11 Disconnected Clients
        } catch (Exception e) {
            LOG.error("Could not schedule timeline calculations.", e);
        }
    }

    @Override
    public String getName() {
        return "TimelinesThread";
    }

}
