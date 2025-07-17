package app.nzyme.core.tables.ethernet;

import app.nzyme.core.rest.resources.taps.reports.tables.arp.ArpPacketReport;
import app.nzyme.core.rest.resources.taps.reports.tables.arp.ArpPacketsReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.joda.time.DateTime;

import java.util.UUID;

public class ARPTable implements DataTable {

    private static final Logger LOG = LogManager.getLogger(ARPTable.class);

    private final Timer totalReportTimer;

    private final TablesService tablesService;

    public ARPTable(TablesService tablesService) {
        this.tablesService = tablesService;

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.ARP_TOTAL_REPORT_PROCESSING_TIMER);
    }

    public void handleReport(UUID tapUuid, DateTime timestamp, ArpPacketsReport report) {
        try (Timer.Context ignored1 = totalReportTimer.time()) {
            tablesService.getNzyme().getDatabase().useHandle(handle -> {
                PreparedBatch insertBatch = handle.prepareBatch("INSERT INTO arp_packets(uuid, tap_uuid, " +
                        "ethernet_source_mac, ethernet_destination_mac, hardware_type, protocol_type, operation, " +
                        "arp_sender_mac, arp_sender_address, arp_target_mac, arp_target_address, size, " +
                        "timestamp, created_at) VALUES(:uuid, :tap_uuid, :ethernet_source_mac, " +
                        ":ethernet_destination_mac, :hardware_type, :protocol_type, :operation, :arp_sender_mac, " +
                        ":arp_sender_address::inet, :arp_target_mac, :arp_target_address::inet, :size, " +
                        ":timestamp, NOW())");

                for (ArpPacketReport packet : report.packets()) {
                    insertBatch
                            .bind("uuid", UUID.randomUUID())
                            .bind("tap_uuid", tapUuid)
                            .bind("ethernet_source_mac", packet.ethernetSourceMac())
                            .bind("ethernet_destination_mac", packet.ethernetDestinationMac())
                            .bind("hardware_type", packet.hardwareType())
                            .bind("protocol_type", packet.protocolType())
                            .bind("operation", packet.operation())
                            .bind("arp_sender_mac", packet.arpSenderMac())
                            .bind("arp_sender_address", packet.arpSenderAddress())
                            .bind("arp_target_mac", packet.arpTargetMac())
                            .bind("arp_target_address", packet.arpTargetAddress())
                            .bind("size", packet.size())
                            .bind("timestamp", packet.timestamp())
                            .add();
                }

                insertBatch.execute();
            });
        }
    }

    @Override
    public void retentionClean() {
        // NOOP. Remove from plugin APIs if there remains no use. Database cleaned by category/tenant independently.
    }
}