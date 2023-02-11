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

package app.nzyme.core.rest.resources.taps.reports.tables.retro.l4;

import app.nzyme.plugin.retro.l4.entries.L4RetroConnectionPairEntry;
import com.google.common.collect.Lists;

import java.util.List;

public class L4RetroReportConverter {

    public static List<L4RetroConnectionPairEntry> pairReportToEntries(String tapName, List<L4RetroPairReport> pairs) {
        List<L4RetroConnectionPairEntry> result = Lists.newArrayList();

        for (L4RetroPairReport pair : pairs) {
            result.add(L4RetroConnectionPairEntry.create(
                    tapName,
                    pair.l4Type(),
                    pair.sourceMac(),
                    pair.destinationMac(),
                    pair.sourceAddress(),
                    pair.destinationAddress(),
                    pair.sourcePort(),
                    pair.destinationPort(),
                    pair.connectionCount(),
                    pair.size(),
                    pair.timestamp()
            ));
        }


        return result;
    }

}
