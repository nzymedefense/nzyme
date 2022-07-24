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

import horse.wtf.nzyme.rest.resources.taps.reports.tables.DNSNxDomainLogReport;
import horse.wtf.nzyme.tables.TablesService;
import org.joda.time.DateTime;

public class DNSTable {

    private final TablesService tablesService;

    public DNSTable(TablesService tablesService) {
        this.tablesService = tablesService;
    }

    public void registerStatistics(String tapName,
                                   String ip,
                                   Long requestCount,
                                   Long requestBytes,
                                   Long responseCount,
                                   Long responseBytes,
                                   Long nxdomainCount,
                                   DateTime timestamp) {

    }

    public void registerNxdomainLog(String tapName,
                                    String ip,
                                    String server,
                                    String queryValue,
                                    String dataType,
                                    DateTime timestamp) {
        tablesService.getNzyme().getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dns_nxdomain_log(tap_name, ip, server, query_value, data_type, " +
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

}
