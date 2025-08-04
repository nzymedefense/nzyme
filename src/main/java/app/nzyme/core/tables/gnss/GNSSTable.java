package app.nzyme.core.tables.gnss;

import app.nzyme.core.rest.resources.taps.reports.tables.gnss.GNSSConstellationsReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import org.joda.time.DateTime;

import java.util.UUID;

public class GNSSTable implements DataTable {

    private final Timer totalReportTimer;

    private final TablesService tablesService;

    public GNSSTable(TablesService tablesService) {
        this.tablesService = tablesService;

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.GNSS_TOTAL_REPORT_PROCESSING_TIMER);
    }

    public void handleConstellationsReport(UUID tapUuid, DateTime timestamp, GNSSConstellationsReport report) {
        try (Timer.Context ignored = totalReportTimer.time()) {

        }
    }

    @Override
    public void retentionClean() {
        // NOOP. Remove from plugin APIs if there remains no use. Database cleaned by category/tenant independently.
    }
}
