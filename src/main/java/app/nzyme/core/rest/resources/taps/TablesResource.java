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
import app.nzyme.core.rest.resources.taps.reports.tables.tcp.TcpSessionReport;
import app.nzyme.core.rest.resources.taps.reports.tables.tcp.TcpSessionsReport;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

        // 802.11
        nzyme.getTablesService().dot11().handleReport(tapId, report.timestamp(), report.dot11());

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

    @POST
    @Path("/tcp/sessions")
    public Response tcpSessions(@Context SecurityContext sc, TcpSessionsReport report) {
        UUID tapId = ((AuthenticatedTap) sc.getUserPrincipal()).getUuid();

        LOG.debug("Received table report from [{}]: {}", tapId, report);

        for (TcpSessionReport session : report.sessions()) {
            LOG.info("SESSION: {}", session);
        }

        return Response.status(Response.Status.CREATED).build();
    }

}
