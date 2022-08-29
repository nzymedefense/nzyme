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

package horse.wtf.nzyme.tables.dns;

import horse.wtf.nzyme.rest.resources.taps.reports.tables.DNSIPStatisticsReport;
import horse.wtf.nzyme.rest.resources.taps.reports.tables.DNSNxDomainLogReport;
import horse.wtf.nzyme.rest.resources.taps.reports.tables.DNSTablesReport;
import horse.wtf.nzyme.tables.DataTable;
import horse.wtf.nzyme.tables.TablesService;
import org.joda.time.DateTime;

import java.util.Map;

public class DNSTable implements DataTable {

    private final TablesService tablesService;

    public DNSTable(TablesService tablesService) {
        this.tablesService = tablesService;
    }

    private void registerStatistics(String tapName,
                                   String ip,
                                   Long requestCount,
                                   Long requestBytes,
                                   Long responseCount,
                                   Long responseBytes,
                                   Long nxdomainCount,
                                   DateTime timestamp) {

        tablesService.getNzyme().getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dns_statistics(tap_name, ip, request_count, request_bytes, " +
                            "response_count, response_bytes, nxdomain_count, created_at) VALUES(:tap_name, :ip, :request_count, " +
                            ":request_bytes, :response_count, :response_bytes, :nxdomain_count, :created_at)")
                        .bind("tap_name", tapName)
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

    private void registerNxdomainLog(String tapName,
                                    String ip,
                                    String server,
                                    String queryValue,
                                    String dataType,
                                    DateTime timestamp) {
        tablesService.getNzyme().getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dns_nxdomains_log(tap_name, ip, server, query_value, data_type, " +
                                "created_at) VALUES(:tap_name, :ip, :server, :query_value, :data_type, :created_at)")
                        .bind("tap_name", tapName)
                        .bind("ip", ip)
                        .bind("server", server)
                        .bind("query_value", queryValue)
                        .bind("data_type", dataType)
                        .bind("created_at", timestamp)
                        .execute()
        );
    }

    private void registerPair(String tapName, String ip, String server, long count, DateTime timestamp) {
        tablesService.getNzyme().getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dns_pairs(tap_name, ip, server, count, created_at) " +
                                "VALUES(:tap_name, :ip, :server, :count, :timestamp)")
                        .bind("tap_name", tapName)
                        .bind("ip", ip)
                        .bind("server", server)
                        .bind("count", count)
                        .bind("timestamp", timestamp)
                        .execute()
        );
    }

    public void handleReport(String tapName, DateTime timestamp, DNSTablesReport report) {
        for (Map.Entry<String, DNSIPStatisticsReport> x : report.ips().entrySet()) {
            DNSIPStatisticsReport stats = x.getValue();

            registerStatistics(
                    tapName,
                    x.getKey(),
                    stats.requestCount(),
                    stats.requestBytes(),
                    stats.responseCount(),
                    stats.responseBytes(),
                    stats.nxDomainCount(),
                    timestamp
            );
        }

        for (DNSNxDomainLogReport nxdomain : report.nxdomains()) {
            if (nxdomain.dataType().equals("PTR")) {
                // We are not interested in reverse lookup NXDOMAINs.
                continue;
            }

            registerNxdomainLog(
                    tapName,
                    nxdomain.ip(),
                    nxdomain.server(),
                    nxdomain.queryValue(),
                    nxdomain.dataType(),
                    timestamp
            );
        }

        for (Map.Entry<String, Map<String, Long>> pair : report.pairs().entrySet()) {
            for (Map.Entry<String, Long> server : pair.getValue().entrySet()) {
                registerPair(tapName, pair.getKey(), server.getKey(), server.getValue(), timestamp);
            }
        }

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
                }
        );
    }
}
