package app.nzyme.core.periodicals.data;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.Dot11RegistryKeys;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.periodicals.distributed.NodeUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class RetentionCleaner extends Periodical {

    private static final Logger LOG = LogManager.getLogger(NodeUpdater.class);

    private final NzymeNode nzyme;

    public RetentionCleaner(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        LOG.info("Running data retention cleaning.");

        // 802.11
        int dot11RetentionDays = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                .getValue(Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.key())
                .orElse(Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING"))
        );
        DateTime dot11CutOff = DateTime.now().minusDays(dot11RetentionDays);

        LOG.info("802.11/WiFi data retention: <{}> days / Delete data older than <{}>.",
                dot11RetentionDays, dot11CutOff);

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM dot11_bssids WHERE created_at < :cutoff")
                        .bind("cutoff", dot11CutOff)
                        .execute()
        );

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM dot11_clients WHERE created_at < :cutoff")
                        .bind("cutoff", dot11CutOff)
                        .execute()
        );
    }

    @Override
    public String getName() {
        return "Data Retention Cleaner";
    }

}
