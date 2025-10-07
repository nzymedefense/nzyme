package app.nzyme.core.tables.ethernet;

import app.nzyme.core.rest.resources.taps.reports.tables.socks.SocksTunnelReport;
import app.nzyme.core.rest.resources.taps.reports.tables.socks.SocksTunnelsReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import app.nzyme.core.util.Tools;
import com.codahale.metrics.Timer;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SOCKSTable implements DataTable {

    private final TablesService tablesService;

    private final Timer totalReportTimer;

    public SOCKSTable(TablesService tablesService) {
        this.tablesService = tablesService;

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.SOCKS_TOTAL_REPORT_PROCESSING_TIMER);
    }

    public void handleReport(UUID tapUuid, DateTime timestamp, SocksTunnelsReport report) {
        tablesService.getNzyme().getDatabase().useHandle(handle -> {
            try(Timer.Context ignored = totalReportTimer.time()) {
                writeTunnels(handle, tapUuid, report.tunnels());
            }
        });
    }

    private void writeTunnels(Handle handle, UUID tapUuid, List<SocksTunnelReport> tunnels) {
        PreparedBatch insertBatch = handle.prepareBatch("INSERT INTO socks_tunnels(uuid, tap_uuid, " +
                "tcp_session_key, socks_type, authentication_status, handshake_status, connection_status, " +
                "username, tunneled_bytes, tunneled_destination_address, tunneled_destination_host, " +
                "tunneled_destination_port, established_at, terminated_at, most_recent_segment_time, " +
                "updated_at, created_at) VALUES(:uuid, :tap_uuid, :tcp_session_key, :socks_type, " +
                ":authentication_status, :handshake_status, :connection_status, :username, :tunneled_bytes, " +
                ":tunneled_destination_address::inet, :tunneled_destination_host, :tunneled_destination_port, " +
                ":established_at, :terminated_at, :most_recent_segment_time, NOW(), NOW())");

        PreparedBatch updateBatch = handle.prepareBatch("UPDATE socks_tunnels SET " +
                "authentication_status = :authentication_status, handshake_status = :handshake_status, " +
                "connection_status = :connection_status, tunneled_bytes = :tunneled_bytes, " +
                "terminated_at = :terminated_at, most_recent_segment_time = :most_recent_segment_time, " +
                "updated_at = NOW() WHERE id = :id");

        for (SocksTunnelReport tunnel : tunnels) {
            String tcpSessionKey = Tools.buildL4Key(
                    tunnel.establishedAt(),
                    tunnel.sourceAddress(),
                    tunnel.destinationAddress(),
                    tunnel.sourcePort(),
                    tunnel.destinationPort());

            Optional<Long> existingTunnel = handle.createQuery("SELECT id FROM socks_tunnels " +
                            "WHERE tcp_session_key = :tcp_session_key AND established_at = :established_at " +
                            "AND tap_uuid = :tap_uuid AND connection_status = :connection_status")
                    .bind("tcp_session_key", tcpSessionKey)
                    .bind("established_at", tunnel.establishedAt())
                    .bind("tap_uuid", tapUuid)
                    .bind("connection_status", "Active")
                    .bind("most_recent_segment_time", tunnel.mostRecentSegmentTime())
                    .mapTo(Long.class)
                    .findOne();

            if (existingTunnel.isEmpty()) {
                insertBatch
                        .bind("uuid", UUID.randomUUID())
                        .bind("tap_uuid", tapUuid)
                        .bind("tcp_session_key", tcpSessionKey)
                        .bind("socks_type", tunnel.socksType().toUpperCase())
                        .bind("authentication_status", tunnel.authenticationStatus())
                        .bind("handshake_status", tunnel.handshakeStatus())
                        .bind("connection_status", tunnel.connectionStatus())
                        .bind("username", tunnel.username())
                        .bind("tunneled_bytes", tunnel.tunneledBytes())
                        .bind("tunneled_destination_address", tunnel.tunneledDestinationAddress())
                        .bind("tunneled_destination_host", tunnel.tunneledDestinationHost())
                        .bind("tunneled_destination_port", tunnel.tunneledDestinationPort())
                        .bind("established_at", tunnel.establishedAt())
                        .bind("terminated_at", tunnel.terminatedAt())
                        .bind("most_recent_segment_time", tunnel.mostRecentSegmentTime())
                        .add();
            } else {
                // Update existing open tunnel.
                updateBatch
                        .bind("id", existingTunnel.get())
                        .bind("authentication_status", tunnel.authenticationStatus())
                        .bind("handshake_status", tunnel.handshakeStatus())
                        .bind("connection_status", tunnel.connectionStatus())
                        .bind("tunneled_bytes", tunnel.tunneledBytes())
                        .bind("terminated_at", tunnel.terminatedAt())
                        .bind("most_recent_segment_time", tunnel.mostRecentSegmentTime())
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
