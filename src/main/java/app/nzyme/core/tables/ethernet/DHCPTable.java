package app.nzyme.core.tables.ethernet;

import app.nzyme.core.ethernet.dhcp.DHCPFingerprint;
import app.nzyme.core.rest.resources.taps.reports.tables.dhcp.DhcpTransactionsReport;
import app.nzyme.core.rest.resources.taps.reports.tables.dhcp.Dhcpv4TransactionReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DHCPTable implements DataTable {

    private static final Logger LOG = LogManager.getLogger(DHCPTable.class);

    private final Timer totalReportTimer;

    private final TablesService tablesService;

    private final ObjectMapper om;

    public DHCPTable(TablesService tablesService) {
        this.tablesService = tablesService;

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DHCP_TOTAL_REPORT_PROCESSING_TIMER);

        this.om = new ObjectMapper()
                .registerModule(new JodaModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void handleReport(UUID tapUuid, DateTime timestamp, DhcpTransactionsReport report) {
        tablesService.getNzyme().getDatabase().useHandle(handle -> {
            try (Timer.Context ignored = totalReportTimer.time()) {
                writeV4Transactions(handle, tapUuid, report.four());
            }
        });
    }

    private void writeV4Transactions(Handle handle, UUID tapUuid, List<Dhcpv4TransactionReport> txs) {
        PreparedBatch insertBatch = handle.prepareBatch("INSERT INTO dhcp_transactions(uuid, tap_uuid, " +
                "transaction_id, transaction_type, client_mac, additional_client_macs, server_mac, " +
                "additional_server_macs, offered_ip_addresses, requested_ip_address, options, additional_options, " +
                "fingerprint, additional_fingerprints, vendor_class, additional_vendor_classes, timestamps, first_packet, latest_packet, notes, is_successful, " +
                "is_complete, updated_at, created_at) VALUES(:uuid, :tap_uuid, :transaction_id, :transaction_type, " +
                ":client_mac, :additional_client_macs::jsonb, :server_mac, :additional_server_macs::jsonb, " +
                ":offered_ip_addresses::jsonb, :requested_ip_address, :options::jsonb, :additional_options::jsonb, :fingerprint, " +
                ":additional_fingerprints::jsonb, :vendor_class, :additional_vendor_classes::jsonb, :timestamps::jsonb, :first_packet, :latest_packet, " +
                ":notes::jsonb, :is_successful, :is_complete, NOW(), NOW())");
        PreparedBatch updateBatch = handle.prepareBatch("UPDATE dhcp_transactions " +
                "SET additional_client_macs = :additional_client_macs::jsonb, server_mac = :server_mac, " +
                "additional_server_macs = :additional_server_macs::jsonb, " +
                "offered_ip_addresses = :offered_ip_addresses::jsonb, " +
                "requested_ip_address = :requested_ip_address, options = :options::jsonb, additional_options = :additional_options::jsonb, " +
                "fingerprint = :fingerprint, additional_fingerprints = :additional_fingerprints::jsonb, vendor_class = :vendor_class, additional_vendor_classes = :additional_vendor_classes::jsonb, " +
                "timestamps = :timestamps::jsonb, " +
                "latest_packet = :latest_packet, notes = :notes::jsonb, is_complete = :is_complete, " +
                "is_successful = :is_successful, updated_at = NOW()");

        for (Dhcpv4TransactionReport tx : txs) {
            String additionalClientMacs;
            String additionalServerMacs;
            String offeredIpAddresses;
            String options;
            String additionalOptions;
            String additionalFingerprints;
            String additionalVendorClasses;
            String timestamps;
            String notes;

            try {
                additionalClientMacs = om.writeValueAsString(tx.additionalClientMacs());
                additionalServerMacs = om.writeValueAsString(tx.additionalServerMacs());
                offeredIpAddresses = om.writeValueAsString(tx.offeredIpAddresses());
                options = om.writeValueAsString(tx.options());
                additionalOptions = om.writeValueAsString(tx.additionalOptions());
                timestamps = om.writeValueAsString(tx.timestamps());
                additionalFingerprints = "[]";
                additionalVendorClasses = om.writeValueAsString(tx.additionalVendorClasses());
                notes = om.writeValueAsString(tx.notes());
            } catch (JsonProcessingException e) {
                LOG.error("Could not serialize DHCP transaction data. Skipping transaction.", e);
                continue;
            }

            Optional<String> fingerprint = new DHCPFingerprint(tx.options(), tx.vendorClass()).generate();

            Optional<Long> existingTx;
            try {
                existingTx = handle.createQuery("SELECT id FROM dhcp_transactions " +
                                "WHERE transaction_id = :transaction_id AND first_packet = :first_packet " +
                                "AND tap_uuid = :tap_uuid AND is_complete = :is_complete")
                        .bind("transaction_id", tx.transactionId())
                        .bind("first_packet", tx.firstPacket())
                        .bind("tap_uuid", tapUuid)
                        .bind("is_complete", false)
                        .mapTo(Long.class)
                        .findOne();
            } catch (IllegalStateException e) {
                LOG.error("Multiple existing DHCP transactions with transaction ID <{}> found. Skipping.",
                        tx.transactionId());
                continue;
            }

            if (existingTx.isEmpty()) {
                insertBatch
                        .bind("uuid", UUID.randomUUID())
                        .bind("tap_uuid", tapUuid)
                        .bind("transaction_id", tx.transactionId())
                        .bind("transaction_type", tx.transactionType())
                        .bind("client_mac", tx.clientMac())
                        .bind("additional_client_macs", additionalClientMacs)
                        .bind("server_mac", tx.serverMac())
                        .bind("additional_server_macs", additionalServerMacs)
                        .bind("offered_ip_addresses", offeredIpAddresses)
                        .bind("requested_ip_address", tx.requestedIpAddress())
                        .bind("options", options)
                        .bind("additional_options", additionalOptions)
                        .bind("fingerprint", fingerprint)
                        .bind("additional_fingerprints", additionalFingerprints)
                        .bind("vendor_class", tx.vendorClass())
                        .bind("additional_vendor_classes", additionalVendorClasses)
                        .bind("timestamps", timestamps)
                        .bind("first_packet", tx.firstPacket())
                        .bind("latest_packet", tx.latestPacket())
                        .bind("notes", notes)
                        .bind("is_successful", tx.successful())
                        .bind("is_complete", tx.complete())
                        .add();
            } else {
                updateBatch
                        .bind("additional_client_macs", additionalClientMacs)
                        .bind("server_mac", tx.serverMac())
                        .bind("additional_server_macs", additionalServerMacs)
                        .bind("offered_ip_addresses", offeredIpAddresses)
                        .bind("requested_ip_address", tx.requestedIpAddress())
                        .bind("options", options)
                        .bind("additional_options", additionalOptions)
                        .bind("fingerprint", fingerprint)
                        .bind("additional_fingerprints", additionalFingerprints)
                        .bind("vendor_class", tx.vendorClass())
                        .bind("additional_vendor_classes", additionalVendorClasses)
                        .bind("timestamps", timestamps)
                        .bind("latest_packet", tx.latestPacket())
                        .bind("notes", notes)
                        .bind("is_successful", tx.successful())
                        .bind("is_complete", tx.complete())
                        .add();
            }
        }

        insertBatch.execute();
        updateBatch.execute();
    }

    @Override
    public void retentionClean() {
        // NOOP. Remove from plugin APIs if there remains no use. Database cleaned by category/tenant independently.
    }

}
