package app.nzyme.core.tables.tcp;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.ethernet.EthernetRegistryKeys;
import app.nzyme.core.ethernet.tcp.TcpSessionState;
import app.nzyme.core.ethernet.tcp.db.TcpSessionEntry;
import app.nzyme.core.integrations.geoip.GeoIpLookupResult;
import app.nzyme.core.integrations.geoip.GeoIpService;
import app.nzyme.core.rest.resources.taps.reports.tables.tcp.TcpSessionReport;
import app.nzyme.core.rest.resources.taps.reports.tables.tcp.TcpSessionsReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import app.nzyme.core.util.Tools;
import com.codahale.metrics.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static app.nzyme.core.util.Tools.stringtoInetAddress;

public class TCPTable implements DataTable {

    private static final Logger LOG = LogManager.getLogger(TCPTable.class);

    private final TablesService tablesService;

    private final Timer totalReportTimer;
    private final Timer sessionsReportTimer;
    private final Timer sessionDiscoveryTimer;

    private final GeoIpService geoIp;

    public TCPTable(TablesService tablesService) {
        this.tablesService = tablesService;

        this.geoIp = tablesService.getNzyme().getGeoIpService();

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.TCP_TOTAL_REPORT_PROCESSING_TIMER);

        this.sessionsReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.TCP_SESSIONS_REPORT_PROCESSING_TIMER);

        this.sessionDiscoveryTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.TCP_SESSION_DISCOVERY_QUERY_TIMER);
    }

    public void handleReport(UUID tapUuid, DateTime timestamp, TcpSessionsReport report) {
        try (Timer.Context ignored = totalReportTimer.time()) {
            try (Timer.Context ignored2 = sessionsReportTimer.time()) {
                CountDownLatch latch = new CountDownLatch(report.sessions().size());
                for (TcpSessionReport session : report.sessions()) {
                    tablesService.getProcessorPool().submit(() -> {
                        writeSession(tapUuid, timestamp, session);

                        latch.countDown();
                    });
                }

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    LOG.error("TCP sessions writer process interrupted.", e);
                }
            }
        }
    }

    private void writeSession(UUID tapUuid, DateTime timestamp, TcpSessionReport session) {
        try {
            String sessionKey = Tools.buildTcpSessionKey(
                    session.startTime(),
                    session.sourceAddress(),
                    session.destinationAddress(),
                    session.sourcePort(),
                    session.destinationPort()
            );

            InetAddress sourceAddress = stringtoInetAddress(session.sourceAddress());
            InetAddress destinationAddress = stringtoInetAddress(session.destinationAddress());
            Optional<GeoIpLookupResult> sourceGeo = geoIp.lookup(sourceAddress);
            Optional<GeoIpLookupResult> destinationGeo = geoIp.lookup(destinationAddress);

            tablesService.getNzyme().getDatabase().useHandle(handle -> {
                Optional<TcpSessionEntry> existingSession;
                try (Timer.Context ignored = sessionDiscoveryTimer.time()) {
                    existingSession = handle.createQuery("SELECT * FROM l4_sessions " +
                                    "WHERE session_key = :session_key AND end_time IS NULL AND tap_uuid = :tap_uuid")
                            .bind("session_key", sessionKey)
                            .bind("tap_uuid", tapUuid)
                            .mapTo(TcpSessionEntry.class)
                            .findOne();
                }

                if (existingSession.isPresent()) {
                    // Existing session. Update.
                    handle.createUpdate("UPDATE l4_sessions SET state = :state, " +
                                    "bytes_count = :bytes_count, segments_count = :segments_count, " +
                                    "end_time = :end_time, most_recent_segment_time = :most_recent_segment_time " +
                                    "WHERE id = :id")
                            .bind("state", TcpSessionState.valueOf(session.state().toUpperCase()))
                            .bind("bytes_count", session.bytesCount())
                            .bind("segments_count", session.segmentsCount())
                            .bind("end_time", session.endTime())
                            .bind("most_recent_segment_time", session.mostRecentSegmentTime())
                            .bind("id", existingSession.get().id())
                            .execute();
                } else {
                    // This is a new session.
                    handle.createUpdate("INSERT INTO l4_sessions(tap_uuid, l4_type, session_key, " +
                                    "source_mac, source_address, source_address_is_site_local, " +
                                    "source_address_is_loopback, source_address_is_multicast, source_port, " +
                                    "destination_mac, destination_address, destination_address_is_site_local, " +
                                    "destination_address_is_loopback, destination_address_is_multicast," +
                                    " destination_port, bytes_count, segments_count, " +
                                    "start_time, end_time, most_recent_segment_time, state, " +
                                    "source_address_geo_asn_number, source_address_geo_asn_name, " +
                                    "source_address_geo_asn_domain, source_address_geo_city, " +
                                    "source_address_geo_country_code, " +
                                    "source_address_geo_latitude, source_address_geo_longitude, " +
                                    "destination_address_geo_asn_number, destination_address_geo_asn_name, " +
                                    "destination_address_geo_asn_domain, destination_address_geo_city, " +
                                    "destination_address_geo_country_code, " +
                                    "destination_address_geo_latitude, destination_address_geo_longitude, " +
                                    "created_at) VALUES(:tap_uuid, :l4_type, :session_key, :source_mac, " +
                                    ":source_address::inet, " +
                                    ":source_address_is_site_local, :source_address_is_loopback, " +
                                    ":source_address_is_multicast, :source_port, :destination_mac, " +
                                    ":destination_address::inet, :destination_address_is_site_local, " +
                                    ":destination_address_is_loopback, :destination_address_is_multicast, " +
                                    ":destination_port, :bytes_count, :segments_count, :start_time, " +
                                    ":end_time, :most_recent_segment_time, :state, " +
                                    ":source_address_geo_asn_number, :source_address_geo_asn_name, " +
                                    ":source_address_geo_asn_domain, :source_address_geo_city, " +
                                    ":source_address_geo_country_code, " +
                                    ":source_address_geo_latitude, :source_address_geo_longitude, " +
                                    ":destination_address_geo_asn_number, :destination_address_geo_asn_name, " +
                                    ":destination_address_geo_asn_domain, :destination_address_geo_city, " +
                                    ":destination_address_geo_country_code, " +
                                    ":destination_address_geo_latitude, :destination_address_geo_longitude, " +
                                    ":created_at)")
                            .bind("tap_uuid", tapUuid)
                            .bind("l4_type", "TCP")
                            .bind("session_key", sessionKey)
                            .bind("source_mac", session.sourceMac())
                            .bind("source_address", session.sourceAddress())
                            .bind("source_address_is_site_local", sourceAddress.isSiteLocalAddress())
                            .bind("source_address_is_loopback", sourceAddress.isLoopbackAddress())
                            .bind("source_address_is_multicast", sourceAddress.isMulticastAddress())
                            .bind("source_port", session.sourcePort())
                            .bind("destination_mac", session.destinationMac())
                            .bind("destination_address", session.destinationAddress())
                            .bind("destination_address_is_site_local", destinationAddress.isSiteLocalAddress())
                            .bind("destination_address_is_loopback", destinationAddress.isLoopbackAddress())
                            .bind("destination_address_is_multicast", destinationAddress.isMulticastAddress())
                            .bind("destination_port", session.destinationPort())
                            .bind("bytes_count", session.bytesCount())
                            .bind("segments_count", session.segmentsCount())
                            .bind("start_time", session.startTime())
                            .bind("end_time", session.endTime())
                            .bind("most_recent_segment_time", session.mostRecentSegmentTime())
                            .bind("state", TcpSessionState.valueOf(session.state().toUpperCase()))
                            .bind("source_address_geo_asn_number", sourceGeo.map(g -> g.asn().number()).orElse(null))
                            .bind("source_address_geo_asn_name", sourceGeo.map(g -> g.asn().name()).orElse(null))
                            .bind("source_address_geo_asn_domain", sourceGeo.map(g -> g.asn().domain()).orElse(null))
                            .bind("source_address_geo_city", sourceGeo.map(g -> g.geo().city()).orElse(null))
                            .bind("source_address_geo_country_code", sourceGeo.map(g -> g.geo().countryCode()).orElse(null))
                            .bind("source_address_geo_latitude", sourceGeo.map(g -> g.geo().latitude()).orElse(null))
                            .bind("source_address_geo_longitude", sourceGeo.map(g -> g.geo().longitude()).orElse(null))
                            .bind("destination_address_geo_asn_number", destinationGeo.map(g -> g.asn().number()).orElse(null))
                            .bind("destination_address_geo_asn_name", destinationGeo.map(g -> g.asn().name()).orElse(null))
                            .bind("destination_address_geo_asn_domain", destinationGeo.map(g -> g.asn().domain()).orElse(null))
                            .bind("destination_address_geo_city", destinationGeo.map(g -> g.geo().city()).orElse(null))
                            .bind("destination_address_geo_country_code", destinationGeo.map(g -> g.geo().countryCode()).orElse(null))
                            .bind("destination_address_geo_latitude", destinationGeo.map(g -> g.geo().latitude()).orElse(null))
                            .bind("destination_address_geo_longitude", destinationGeo.map(g -> g.geo().longitude()).orElse(null))
                            .bind("created_at", timestamp)
                            .execute();
                }
            });
        } catch(Exception e) {
            LOG.error("Could not write TCP session.", e);
        }
    }

    @Override
    public void retentionClean() {
        NzymeNode nzyme = tablesService.getNzyme();
        int l4RetentionDays = Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                .getValue(EthernetRegistryKeys.L4_RETENTION_TIME_DAYS.key())
                .orElse(EthernetRegistryKeys.L4_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING"))
        );
        DateTime l4CutOff = DateTime.now().minusDays(l4RetentionDays);

        LOG.info("Ethernet/L4 data retention: <{}> days / Delete data older than <{}>.",
                l4RetentionDays, l4CutOff);

        nzyme.getDatabase().useHandle(handle -> {
            handle.createUpdate("DELETE FROM l4_sessions WHERE most_recent_segment_time < :cutoff")
                    .bind("cutoff", l4CutOff)
                    .execute();
        });
    }
}
