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

package horse.wtf.nzyme.rest.resources;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.reporting.Report;
import horse.wtf.nzyme.reporting.reports.TacticalSummaryReport;
import horse.wtf.nzyme.rest.authentication.Secured;
import horse.wtf.nzyme.rest.requests.ScheduleReportRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/reports")
@Secured
@Produces(MediaType.APPLICATION_JSON)
public class ReportsResource {

    private static final Logger LOG = LogManager.getLogger(TrackersResource.class);

    @Inject
    private NzymeLeader nzyme;

    @POST
    @Path("/schedule")
    public Response scheduleReport(ScheduleReportRequest request) {

        // TODO XXX: Email receivers

        Report report;
        switch (request.reportType()) {
            case "TacticalSummary":
                report = new TacticalSummaryReport(request.hourOfDay(), request.minuteOfHour());
                break;
            default:
                LOG.error("No report of type [{}] found", request.reportType());
                return Response.status(404).build();
        }

        try {
            nzyme.getSchedulingService().scheduleReport(report);
            return Response.status(Response.Status.CREATED).build();
        } catch(SchedulerException e) {
            throw new RuntimeException("Could not schedule report.", e);
        }

    }

}
