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

import app.nzyme.core.rest.resources.taps.reports.tables.DNSIPStatisticsReport;
import app.nzyme.core.rest.resources.taps.reports.tables.DNSNxDomainLogReport;
import app.nzyme.core.rest.resources.taps.reports.tables.DNSTablesReport;
import app.nzyme.core.tables.DataTable;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.Timer;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.UUID;

public class DNSTable implements DataTable {

    private final TablesService tablesService;

    private final Timer totalReportTimer;
    private final Timer statisticsReportTimer;
    private final Timer nxdomainsReportTimer;
    private final Timer pairsReportTimer;

    public DNSTable(TablesService tablesService) {
        this.tablesService = tablesService;

        this.totalReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DNS_TOTAL_REPORT_PROCESSING_TIMER);
        this.statisticsReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DNS_STATISTICS_REPORT_PROCESSING_TIMER);
        this.nxdomainsReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DNS_NXDOMAINS_REPORT_PROCESSING_TIMER);
        this.pairsReportTimer = tablesService.getNzyme().getMetrics()
                .timer(MetricNames.DNS_PAIRS_REPORT_PROCESSING_TIMER);
    }

    public void handleReport(UUID tapUuid, DateTime timestamp, DNSTablesReport report) {
        try (Timer.Context ignored = totalReportTimer.time()) {
            try (Timer.Context ignored2 = statisticsReportTimer.time()) {
                for (Map.Entry<String, DNSIPStatisticsReport> x : report.ips().entrySet()) {
                    DNSIPStatisticsReport stats = x.getValue();

                    registerStatistics(
                            tapUuid,
                            x.getKey(),
                            stats.requestCount(),
                            stats.requestBytes(),
                            stats.responseCount(),
                            stats.responseBytes(),
                            stats.nxDomainCount(),
                            timestamp
                    );
                }
            }

            try (Timer.Context ignored2 = nxdomainsReportTimer.time()) {
                for (DNSNxDomainLogReport nxdomain : report.nxdomains()) {
                    if (nxdomain.dataType().equals("PTR")) {
                        // We are not interested in reverse lookup NXDOMAINs.
                        continue;
                    }

                    registerNxdomainLog(
                            tapUuid,
                            nxdomain.ip(),
                            nxdomain.server(),
                            nxdomain.queryValue(),
                            nxdomain.dataType(),
                            timestamp
                    );
                }
            }

            try (Timer.Context ignored2 = pairsReportTimer.time()) {
                for (Map.Entry<String, Map<String, Long>> pair : report.pairs().entrySet()) {
                    for (Map.Entry<String, Long> server : pair.getValue().entrySet()) {
                        registerPair(tapUuid, pair.getKey(), server.getKey(), server.getValue(), timestamp);
                    }
                }
            }
        }
    }

    private void registerStatistics(UUID tapUuid,
                                    String ip,
                                    Long requestCount,
                                    Long requestBytes,
                                    Long responseCount,
                                    Long responseBytes,
                                    Long nxdomainCount,
                                    DateTime timestamp) {

        tablesService.getNzyme().getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dns_statistics(tap_uuid, ip, request_count, request_bytes, " +
                            "response_count, response_bytes, nxdomain_count, created_at) VALUES(:tap_uuid, :ip, :request_count, " +
                            ":request_bytes, :response_count, :response_bytes, :nxdomain_count, :created_at)")
                        .bind("tap_uuid", tapUuid)
                        .bind("ip", ip)
                        .bind("request_count", requestCount)
                        .bind("request_bytes", requestBytes)
                        .bind("response_count", responseCount)
                        .bind("response_bytes", responseBytes)
                        .bind("nxdomain_count", nxdomainCount)
                        .bind("created_at", timestamp)
                        .execute()
        );

    }

    private void registerNxdomainLog(UUID tapUuid,
                                    String ip,
                                    String server,
                                    String queryValue,
                                    String dataType,
                                    DateTime timestamp) {
        tablesService.getNzyme().getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dns_nxdomains_log(tap_uuid, ip, server, query_value, data_type, " +
                                "created_at) VALUES(:tap_uuid, :ip, :server, :query_value, :data_type, :created_at)")
                        .bind("tap_uuid", tapUuid)
                        .bind("ip", ip)
                        .bind("server", server)
                        .bind("query_value", queryValue)
                        .bind("data_type", dataType)
                        .bind("created_at", timestamp)
                        .execute()
        );
    }

    private void registerPair(UUID tapUuid, String ip, String server, long count, DateTime timestamp) {
        tablesService.getNzyme().getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dns_pairs(tap_uuid, ip, server, count, created_at) " +
                                "VALUES(:tap_uuid, :ip, :server, :count, :timestamp)")
                        .bind("tap_uuid", tapUuid)
                        .bind("ip", ip)
                        .bind("server", server)
                        .bind("count", count)
                        .bind("timestamp", timestamp)
                        .execute()
        );
    }

    @Override
    public void retentionClean() {
        tablesService.getNzyme().getDatabase().useHandle(handle -> {
            handle.createUpdate("DELETE FROM dns_statistics WHERE created_at < :created_at")
                    .bind("created_at", DateTime.now().minusHours(24)) // TODO
                    .execute();

            handle.createUpdate("DELETE FROM dns_nxdomains_log WHERE created_at < :created_at")
                    .bind("created_at", DateTime.now().minusHours(24)) // TODO
                    .execute();

            handle.createUpdate("DELETE FROM dns_pairs WHERE created_at < :created_at")
                    .bind("created_at", DateTime.now().minusHours(24)) // TODO
                    .execute();
        });
    }
}
