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

package app.nzyme.core.rest.resources.taps;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.authentication.AuthenticatedTap;
import app.nzyme.core.rest.authentication.TapSecured;
import app.nzyme.core.rest.resources.taps.reports.tables.TablesReport;
import app.nzyme.core.rest.resources.taps.reports.tables.retro.dns.DNSRetroReportConverter;
import app.nzyme.core.rest.resources.taps.reports.tables.retro.l4.L4RetroReportConverter;
import app.nzyme.core.security.authentication.db.TapPermissionEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

@Path("/api/taps/tables")
@TapSecured
@Produces(MediaType.APPLICATION_JSON)
public class TablesResource {

    private static final Logger LOG = LogManager.getLogger(StatusResource.class);

    @Inject
    private NzymeNode nzyme;

    @POST
    public Response report(@Context SecurityContext sc, TablesReport report) {
        UUID tapId = ((AuthenticatedTap) sc.getUserPrincipal()).getUuid();

        LOG.debug("Received table report from [{}]: {}", tapId, report);

        // DNS.
        nzyme.getTablesService().dns().handleReport(tapId, report.timestamp(), report.dns());

        // Submit to Retro if service is present. TODO move to tap UUIDs instead of names.
        /*if (nzyme.retroService().isPresent()) {
            // TODO queue this. Don't wait for completion.
            nzyme.retroService().get().l4().handleL4ConnectionPairReport(
                    L4RetroReportConverter.pairReportToEntries(report.tapName(), report.l4().retroPairs())
            );

            nzyme.retroService().get().dns().handleQueryLogReport(
                    DNSRetroReportConverter.queryReportToEntries(report.tapName(), report.dns().retroQueries())
            );

            nzyme.retroService().get().dns().handleResponseLogReport(
                    DNSRetroReportConverter.responseReportToEntries(report.tapName(), report.dns().retroResponses())
            );
        }*/

        return Response.status(Response.Status.CREATED).build();
    }

}
