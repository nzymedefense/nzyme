package app.nzyme.core.tables.dot11;

import app.nzyme.core.rest.resources.taps.reports.tables.dot11.Dot11TablesReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.UUID;

public class Dot11Table implements DataTable {

    private static final Logger LOG = LogManager.getLogger(Dot11Table.class);

    private final TablesService tablesService;

    public Dot11Table(TablesService tablesService) {
        this.tablesService = tablesService;
    }

    public void handleReport(UUID tapUuid, DateTime timestamp, Dot11TablesReport report) {
        LOG.info(report);
    }

    @Override
    public void retentionClean() {
        // called to retention clean db tables
    }

}
