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
import app.nzyme.core.rest.resources.taps.reports.tables.DNSTablesReport;
import app.nzyme.core.rest.resources.taps.reports.tables.dot11.Dot11TablesReport;
import app.nzyme.core.rest.resources.taps.reports.tables.tcp.TcpSessionsReport;
import app.nzyme.core.rest.resources.taps.reports.tables.udp.UdpDatagramsReport;
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
import org.joda.time.DateTime;

import java.util.UUID;

@Path("/api/taps/tables")
@TapSecured
@Produces(MediaType.APPLICATION_JSON)
public class TablesResource {

    private static final Logger LOG = LogManager.getLogger(StatusResource.class);

    @Inject
    private NzymeNode nzyme;

    /*
     * REMEMBER: The `TapTableSizeInterceptor` will consider all requests that include `api/taps/tables` in
     * their request URI.
     */

    @POST
    @Path("/dot11/summary")
    public Response dot11Summary(@Context SecurityContext sc, Dot11TablesReport report) {
        UUID tapId = ((AuthenticatedTap) sc.getUserPrincipal()).getUuid();

        LOG.debug("Received 802.11 summary report from [{}]: {}", tapId, report);
        nzyme.getTablesService().dot11().handleReport(tapId, DateTime.now(), report);

        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/tcp/sessions")
    public Response tcpSessions(@Context SecurityContext sc, TcpSessionsReport report) {
        UUID tapId = ((AuthenticatedTap) sc.getUserPrincipal()).getUuid();

        LOG.debug("Received TCP session table report from [{}]: {}", tapId, report);
        nzyme.getTablesService().tcp().handleReport(tapId, DateTime.now(), report);

        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/udp/datagrams")
    public Response udpDatagrams(@Context SecurityContext sc, UdpDatagramsReport report) {
        UUID tapId = ((AuthenticatedTap) sc.getUserPrincipal()).getUuid();

        LOG.debug("Received UDP datagram table report from [{}]: {}", tapId, report);

        // Store in combined TCP/UDP table.

        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/dns/summary")
    public Response dnsSummary(@Context SecurityContext sc, DNSTablesReport report) {
        UUID tapId = ((AuthenticatedTap) sc.getUserPrincipal()).getUuid();

        LOG.debug("Received DNS summary report from [{}]: {}", tapId, report);
        nzyme.getTablesService().dns().handleReport(tapId, DateTime.now(), report);

        return Response.status(Response.Status.CREATED).build();
    }

}
