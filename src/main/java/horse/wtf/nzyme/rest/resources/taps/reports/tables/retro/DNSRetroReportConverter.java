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

package horse.wtf.nzyme.rest.resources.taps.reports.tables.retro;

import app.nzyme.plugin.retro.dns.entries.DNSRetroQueryLogEntry;
import app.nzyme.plugin.retro.dns.entries.DNSRetroResponseLogEntry;
import com.google.common.collect.Lists;

import java.util.List;

public class DNSRetroReportConverter {

    public static List<DNSRetroQueryLogEntry> queryReportToEntries(String tapName, List<DNSRetroQueryLogReport> report) {
        List<DNSRetroQueryLogEntry> result = Lists.newArrayList();

        for (DNSRetroQueryLogReport r : report) {
            result.add(DNSRetroQueryLogEntry.create(
                    tapName,
                    r.ip(),
                    r.server(),
                    r.sourceMac(),
                    r.destinationMac(),
                    r.port(),
                    r.queryValue(),
                    r.dataType(),
                    r.timestamp()
            ));
        }

        return result;
    }

    public static List<DNSRetroResponseLogEntry> responseReportToEntries(String tapName, List<DNSRetroResponseLogReport> report) {
        List<DNSRetroResponseLogEntry> result = Lists.newArrayList();

        for (DNSRetroResponseLogReport r : report) {
            result.add(DNSRetroResponseLogEntry.create(
                    tapName,
                    r.ip(),
                    r.server(),
                    r.sourceMac(),
                    r.destinationMac(),
                    r.responseValue(),
                    r.dataType(),
                    r.timestamp()
            ));
        }

        return result;
    }

}
