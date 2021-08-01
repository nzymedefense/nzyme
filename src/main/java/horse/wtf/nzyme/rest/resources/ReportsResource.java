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

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.reporting.Report;
import horse.wtf.nzyme.reporting.db.ScheduledReportEntry;
import horse.wtf.nzyme.reporting.reports.TacticalSummaryReport;
import horse.wtf.nzyme.rest.authentication.Secured;
import horse.wtf.nzyme.rest.requests.ScheduleReportRequest;
import horse.wtf.nzyme.rest.responses.reports.ScheduledReportEntryResponse;
import horse.wtf.nzyme.rest.responses.reports.ScheduledReportsListResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Path("/api/reports")
@Secured
@Produces(MediaType.APPLICATION_JSON)
public class ReportsResource {

    private static final Logger LOG = LogManager.getLogger(TrackersResource.class);

    @Inject
    private NzymeLeader nzyme;

    @GET
    public Response findAllScheduledReports() {
        List<ScheduledReportEntryResponse> reports = Lists.newArrayList();
        for (ScheduledReportEntry report : nzyme.getSchedulingService().findAllScheduledReports()) {
            reports.add(entryToResponse(report));
        }

        return Response.ok(ScheduledReportsListResponse.create(reports.size(), reports)).build();
    }

    @GET
    @Path("/show/{name}")
    public Response findReport(@PathParam("name") String id) {
        Optional<ScheduledReportEntry> result = nzyme.getSchedulingService().findScheduledReport(id);

        if (result.isEmpty()) {
            return Response.status(404).build();
        }

        return Response.ok(entryToResponse(result.get())).build();
    }

    @POST
    @Path("/schedule")
    public Response scheduleReport(ScheduleReportRequest request) {
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
            String dbName = nzyme.getSchedulingService().scheduleReport(report);

            for (String email : request.emailReceivers()) {
                nzyme.getSchedulingService().addEmailReceiverToReport(dbName, email);
            }

            return Response.status(Response.Status.CREATED).build();
        } catch(SchedulerException e) {
            throw new RuntimeException("Could not schedule report.", e);
        }

    }

    private ScheduledReportEntryResponse entryToResponse(ScheduledReportEntry x) {
        CronDescriptor cronDescriptor = CronDescriptor.instance(Locale.getDefault());
        CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

        return ScheduledReportEntryResponse.create(
                x.name().split("-")[1],
                x.name(),
                x.nextFireTime(),
                x.previousFireTime(),
                x.triggerState(),
                x.cronExpression(),
                cronDescriptor.describe(cronParser.parse(x.cronExpression()))
        );
    }

}
