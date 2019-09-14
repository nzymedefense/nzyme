package horse.wtf.nzyme.periodicals.sigidx;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SignalIndexHistogramCleaner extends Periodical {

    private static final Logger LOG = LogManager.getLogger(SignalIndexHistogramCleaner.class);

    private final Database database;

    public SignalIndexHistogramCleaner(Nzyme nzyme) {
        this.database = nzyme.getDatabase();
    }

    @Override
    protected void execute() {
        try {
            LOG.debug("Retention cleaning signal index history values.");

            database.useHandle(handle -> {
                handle.execute("DELETE FROM sigidx_histogram_history WHERE created_at < (current_timestamp at time zone 'UTC' - interval '8 hours')");
            });
        } catch(Exception e) {
            LOG.error("Could not retention clean signal index history information.", e);
        }
    }

    @Override
    public String getName() {
        return "SignalIndexHistogramCleaner";
    }

}
