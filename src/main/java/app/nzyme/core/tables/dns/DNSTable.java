/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.tables.dns;

import app.nzyme.core.integrations.geoip.GeoIpLookupResult;
import app.nzyme.core.rest.resources.taps.reports.tables.DNSIPStatisticsReport;
import app.nzyme.core.rest.resources.taps.reports.tables.DNSTablesReport;
import app.nzyme.core.rest.resources.taps.reports.tables.dns.DNSLogReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.joda.time.DateTime;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DNSTable implements DataTable {

    private final TablesService tablesService;

    private final Timer totalReportTimer;
    private final Timer statisticsReportTimer;
    private final Timer pairsReportTimer;
    private final Timer logReportTimer;

    public DNSTable(TablesService tablesService) {
        this.tablesService = tablesService;

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DNS_TOTAL_REPORT_PROCESSING_TIMER);
        this.statisticsReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DNS_STATISTICS_REPORT_PROCESSING_TIMER);
        this.pairsReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DNS_PAIRS_REPORT_PROCESSING_TIMER);
        this.logReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DNS_LOG_REPORT_PROCESSING_TIMER);
    }

    public void handleReport(UUID tapUuid, DateTime timestamp, DNSTablesReport report) {
        try (Timer.Context ignored = totalReportTimer.time()) {
            tablesService.getNzyme().getDatabase().useHandle(handle -> {
                try (Timer.Context ignored2 = statisticsReportTimer.time()) {
                    registerStatistics(handle, tapUuid, report.ips(), timestamp);
                }

                try (Timer.Context ignored2 = pairsReportTimer.time()) {
                    registerPairs(handle, tapUuid, report.pairs(), timestamp);
                }

                try (Timer.Context ignored2 = logReportTimer.time()) {
                    registerLogs(handle, tapUuid, report.queryLog(), report.responseLog());
                }
            });
        }
    }

    private void registerStatistics(Handle handle,
                                    UUID tapUuid,
                                    Map<String, DNSIPStatisticsReport> m,
                                    DateTime timestamp) {
        PreparedBatch batch = handle.prepareBatch("INSERT INTO dns_statistics(tap_uuid, ip, request_count, " +
                "request_bytes, response_count, response_bytes, nxdomain_count, created_at) VALUES(:tap_uuid, :ip, " +
                ":request_count, :request_bytes, :response_count, :response_bytes, :nxdomain_count, :created_at)");

        for (Map.Entry<String, DNSIPStatisticsReport> x : m.entrySet()) {
            String ip = x.getKey();
            DNSIPStatisticsReport stats = x.getValue();

            batch.bind("tap_uuid", tapUuid)
                    .bind("ip", ip)
                    .bind("request_count", stats.requestCount())
                    .bind("request_bytes", stats.requestBytes())
                    .bind("response_count", stats.responseCount())
                    .bind("response_bytes", stats.responseBytes())
                    .bind("nxdomain_count", stats.nxDomainCount())
                    .bind("created_at", timestamp)
                    .add();
        }

        batch.execute();
    }

    private void registerPairs(Handle handle, UUID tapUuid, Map<String, Map<String, Long>> pairs, DateTime timestamp) {
        PreparedBatch batch = handle.prepareBatch("INSERT INTO dns_pairs(tap_uuid, ip, server, " +
                "server_geo_asn_number, server_geo_asn_name, server_geo_asn_domain, server_geo_city, " +
                "server_geo_country_code, server_geo_latitude, server_geo_longitude, count, created_at) " +
                "VALUES(:tap_uuid, :ip, :server, :server_geo_asn_number, :server_geo_asn_name, " +
                ":server_geo_asn_domain, :server_geo_city, :server_geo_country_code, :server_geo_latitude, " +
                ":server_geo_longitude, :count, :timestamp)");

        for (Map.Entry<String, Map<String, Long>> pair : pairs.entrySet()) {
            for (Map.Entry<String, Long> server : pair.getValue().entrySet()) {
                InetAddress serverAddress;
                try {
                    serverAddress = InetAddress.getByName(server.getKey());
                } catch (UnknownHostException e) {
                    // This shouldn't happen because we pass IP addresses.
                    throw new RuntimeException(e);
                }

                Optional<GeoIpLookupResult> geo = tablesService.getNzyme().getGeoIpService().lookup(serverAddress);

                batch.bind("tap_uuid", tapUuid)
                        .bind("ip", pair.getKey())
                        .bind("server", server.getKey())
                        .bind("server_geo_asn_number", geo.map(g -> g.asn().number()).orElse(null))
                        .bind("server_geo_asn_name", geo.map(g -> g.asn().name()).orElse(null))
                        .bind("server_geo_asn_domain", geo.map(g -> g.asn().domain()).orElse(null))
                        .bind("server_geo_city", geo.map(g -> g.geo().city()).orElse(null))
                        .bind("server_geo_country_code", geo.map(g -> g.geo().countryCode()).orElse(null))
                        .bind("server_geo_latitude", geo.map(g -> g.geo().latitude()).orElse(null))
                        .bind("server_geo_longitude", geo.map(g -> g.geo().longitude()).orElse(null))
                        .bind("count", server.getValue())
                        .bind("timestamp", timestamp)
                        .add();
            }
        }

        batch.execute();
    }

    private void registerLogs(Handle handle,
                              UUID tapUuid,
                              List<DNSLogReport> queries,
                              List<DNSLogReport> responses) {
        PreparedBatch batch = handle.prepareBatch("INSERT INTO dns_log(uuid, tap_uuid, transaction_id, " +
                "dns_type, client_address, client_port, client_mac, server_address, server_port, server_mac, " +
                "data_value, data_type,  timestamp, created_at) VALUES(:uuid, :tap_uuid, :transaction_id, :dns_type, " +
                ":client_address, :client_port, :client_mac, :server_address, :server_port, :server_mac, " +
                ":data_value, :data_type, :timestamp, NOW())");

        for (DNSLogReport d : queries) {
            batch
                    .bind("uuid", UUID.randomUUID())
                    .bind("tap_uuid", tapUuid)
                    .bind("transaction_id", d.transactionId())
                    .bind("dns_type", "query")
                    .bind("client_address", d.clientAddress())
                    .bind("client_port", d.clientPort())
                    .bind("client_mac", d.clientMac())
                    .bind("server_address", d.serverAddress())
                    .bind("server_port", d.serverPort())
                    .bind("server_mac", d.serverMac())
                    .bind("data_value", d.dataValue())
                    .bind("data_type", d.dataType())
                    .bind("timestamp", d.timestamp())
                    .add();
        }

        for (DNSLogReport d : responses) {
            batch
                    .bind("uuid", UUID.randomUUID())
                    .bind("tap_uuid", tapUuid)
                    .bind("transaction_id", d.transactionId())
                    .bind("dns_type", "response")
                    .bind("client_address", d.clientAddress())
                    .bind("client_port", d.clientPort())
                    .bind("client_mac", d.clientMac())
                    .bind("server_address", d.serverAddress())
                    .bind("server_port", d.serverPort())
                    .bind("server_mac", d.serverMac())
                    .bind("data_value", d.dataValue())
                    .bind("data_type", d.dataType())
                    .bind("timestamp", d.timestamp())
                    .add();
        }

        batch.execute();
    }

    @Override
    public void retentionClean() {
        tablesService.getNzyme().getDatabase().useHandle(handle -> {
            handle.createUpdate("DELETE FROM dns_statistics WHERE created_at < :created_at")
                    .bind("created_at", DateTime.now().minusHours(24)) // TODO
                    .execute();

            handle.createUpdate("DELETE FROM dns_pairs WHERE created_at < :created_at")
                    .bind("created_at", DateTime.now().minusHours(24)) // TODO
                    .execute();

            handle.createUpdate("DELETE FROM dns_logs WHERE created_at < :created_at")
                    .bind("created_at", DateTime.now().minusHours(24)) // TODO
                    .execute();
        });
    }
}
