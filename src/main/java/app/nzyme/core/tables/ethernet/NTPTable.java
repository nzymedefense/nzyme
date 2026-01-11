package app.nzyme.core.tables.ethernet;

import app.nzyme.core.rest.resources.taps.reports.tables.ntp.NTPTransactionReport;
import app.nzyme.core.rest.resources.taps.reports.tables.ntp.NTPTransactionsReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import app.nzyme.core.util.Tools;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.joda.time.DateTime;

import java.util.UUID;

public class NTPTable implements DataTable {

    private static final Logger LOG = LogManager.getLogger(NTPTable.class);

    private final Timer totalReportTimer;

    private final TablesService tablesService;
    private final ObjectMapper om;

    public NTPTable(TablesService tablesService) {
        this.tablesService = tablesService;

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.NTP_TOTAL_REPORT_PROCESSING_TIMER);

        this.om = new ObjectMapper()
                .registerModule(new JodaModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void handleReport(UUID tapUuid, DateTime timestamp, NTPTransactionsReport report) {
        tablesService.getNzyme().getDatabase().useHandle(handle -> {
            try (Timer.Context ignored1 = totalReportTimer.time()) {
                PreparedBatch batch = handle.prepareBatch("INSERT INTO ntp_transactions(tap_uuid, " +
                        "transaction_key, complete, notes, client_mac, server_mac, client_address, server_address, " +
                        "client_port, server_port, request_size, response_size, timestamp_client_transmit, " +
                        "timestamp_server_receive, timestamp_server_transmit, timestamp_client_tap_receive, " +
                        "timestamp_server_tap_receive, server_version, client_version, server_mode, client_mode, " +
                        "stratum, leap_indicator, precision, poll_interval, root_delay_seconds, " +
                        "root_dispersion_seconds, reference_id, delay_seconds, offset_seconds, rtt_seconds, " +
                        "server_processing_seconds, created_at) " +
                        "VALUES(:tap_uuid, :transaction_key, :complete, :notes::jsonb, :client_mac, :server_mac, " +
                        ":client_address::inet, :server_address::inet, :client_port, :server_port, :request_size, " +
                        ":response_size, :timestamp_client_transmit, :timestamp_server_receive, " +
                        ":timestamp_server_transmit, :timestamp_client_tap_receive, :timestamp_server_tap_receive, " +
                        ":server_version, :client_version, :server_mode, :client_mode, :stratum, :leap_indicator, " +
                        ":precision, :poll_interval, :root_delay_seconds, :root_dispersion_seconds, " +
                        ":reference_id, :delay_seconds, :offset_seconds, :rtt_seconds, " +
                        ":server_processing_seconds, NOW())");

                for (NTPTransactionReport tx : report.transactions()) {
                    String notes;
                    if (tx.notes() != null && !tx.notes().isEmpty()) {
                        try {
                            notes = om.writeValueAsString(tx.notes());
                        } catch (JsonProcessingException e) {
                            LOG.error("Failed to serialize NTP transaction notes. Setting to NULL.", e);
                            notes = null;
                        }
                    } else {
                        notes = null;
                    }

                    DateTime l4KeyTimestamp;
                    if (tx.timestampClientTransmit() != null) {
                        l4KeyTimestamp = tx.timestampClientTapReceive();
                    } else {
                        l4KeyTimestamp = tx.timestampServerTapReceive();
                    }

                    if (l4KeyTimestamp == null) {
                        LOG.error("Both client and server transmit timestamp are NULL.");
                        continue;
                    }

                    String transactionKey = Tools.buildL4Key(
                            l4KeyTimestamp,
                            tx.clientAddress(),
                            tx.serverAddress(),
                            tx.clientPort(),
                            tx.serverPort()
                    );

                    batch.bind("tap_uuid", tapUuid)
                            .bind("transaction_key", transactionKey)
                            .bind("complete", tx.complete())
                            .bind("notes", notes)
                            .bind("client_mac", tx.clientMac())
                            .bind("server_mac", tx.serverMac())
                            .bind("client_address", tx.clientAddress())
                            .bind("server_address", tx.serverAddress())
                            .bind("client_port", tx.clientPort())
                            .bind("server_port", tx.serverPort())
                            .bind("request_size", tx.requestSize())
                            .bind("response_size", tx.responseSize())
                            .bind("timestamp_client_transmit", tx.timestampClientTransmit())
                            .bind("timestamp_server_receive", tx.timestampServerReceive())
                            .bind("timestamp_server_transmit", tx.timestampServerTransmit())
                            .bind("timestamp_client_tap_receive", tx.timestampClientTapReceive())
                            .bind("timestamp_server_tap_receive", tx.timestampServerTapReceive())
                            .bind("server_version", tx.serverVersion())
                            .bind("client_version", tx.clientVersion())
                            .bind("server_mode", tx.serverMode())
                            .bind("client_mode", tx.clientMode())
                            .bind("stratum", tx.stratum())
                            .bind("leap_indicator", tx.leapIndicator())
                            .bind("precision", tx.precision())
                            .bind("poll_interval", tx.pollInterval())
                            .bind("root_delay_seconds", tx.rootDelaySeconds())
                            .bind("root_dispersion_seconds", tx.rootDispersionSeconds())
                            .bind("reference_id", tx.referenceId())
                            .bind("delay_seconds", tx.delaySeconds())
                            .bind("offset_seconds", tx.offsetSeconds())
                            .bind("rtt_seconds", tx.rttSeconds())
                            .bind("server_processing_seconds", tx.serverProcessingSeconds())
                            .add();
                }

                batch.execute();
            }
        });
    }

    @Override
    public void retentionClean() {
        // NOOP. Remove from plugin APIs if there remains no use. Database cleaned by category/tenant independently.
    }

}
