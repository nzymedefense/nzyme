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
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.reporting.Report;
import horse.wtf.nzyme.reporting.db.ExecutionLogEntry;
import horse.wtf.nzyme.reporting.db.ScheduledReportEntry;
import horse.wtf.nzyme.reporting.reports.TacticalSummaryReport;
import horse.wtf.nzyme.rest.authentication.Secured;
import horse.wtf.nzyme.rest.requests.ModifyReportReceiverEmailRequest;
import horse.wtf.nzyme.rest.requests.ScheduleReportRequest;
import horse.wtf.nzyme.rest.responses.reports.ExecutionLogEntryResponse;
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
            List<ExecutionLogEntry> logs = nzyme.getSchedulingService().findExecutionLogs(report.name());

            reports.add(entryToResponse(report, logs));
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

        List<ExecutionLogEntry> logs = nzyme.getSchedulingService().findExecutionLogs(result.get().name());

        return Response.ok(entryToResponse(result.get(), logs)).build();
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

            List<String> addedEmailReceivers = Lists.newArrayList();
            for (String email : request.emailReceivers()) {
                if (addedEmailReceivers.contains(email)) {
                    LOG.warn("Duplicate email address. Skipping.");
                    continue;
                }
                nzyme.getSchedulingService().addEmailReceiverToReport(dbName, email);
                addedEmailReceivers.add(email);
            }

            return Response.status(Response.Status.CREATED).build();
        } catch(SchedulerException e) {
            throw new RuntimeException("Could not schedule report.", e);
        }

    }

    @POST
    @Path("/show/{name}/receivers/email")
    public Response addEmailReceiver(@PathParam("name") String id, ModifyReportReceiverEmailRequest request) {
        Optional<ScheduledReportEntry> result = nzyme.getSchedulingService().findScheduledReport(id);

        if (result.isEmpty()) {
            return Response.status(404).build();
        }

        ScheduledReportEntry report = result.get();
        if (nzyme.getSchedulingService().findEmailReceiversOfReport(report.name()).contains(request.emailAddress())) {
            LOG.error("Email address already exists in receivers for this report. Skipping.");
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        nzyme.getSchedulingService().addEmailReceiverToReport(report.name(), request.emailAddress());

        return Response.status(Response.Status.CREATED).build();
    }

    @POST // not using DELETE because libraries are inconsistent with allowing bodies in DELETE requests.
    @Path("/show/{name}/receivers/email/delete")
    public Response removeEmailReceiver(@PathParam("name") String id, ModifyReportReceiverEmailRequest request) {
        Optional<ScheduledReportEntry> result = nzyme.getSchedulingService().findScheduledReport(id);

        if (result.isEmpty()) {
            return Response.status(404).build();
        }

        nzyme.getSchedulingService().removeEmailReceiverFromReport(result.get().name(), request.emailAddress());

        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/show/{name}")
    public Response deleteReport(@PathParam("name") String id) {
        Optional<ScheduledReportEntry> result = nzyme.getSchedulingService().findScheduledReport(id);

        if (result.isEmpty()) {
            return Response.status(404).build();
        }

        try {
            nzyme.getSchedulingService().unscheduleAndDeleteReport(id);
        } catch(Exception e) {
            LOG.error("Could not delete report.", e);
            return Response.serverError().build();
        }

        return Response.ok().build();
    }


    private ScheduledReportEntryResponse entryToResponse(ScheduledReportEntry x, List<ExecutionLogEntry> executionLog) {
        CronDescriptor cronDescriptor = CronDescriptor.instance(Locale.getDefault());
        CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

        List<ExecutionLogEntryResponse> executionLogResponse = Lists.newArrayList();
        if (executionLog != null) {
            for (ExecutionLogEntry log : executionLog) {
                executionLogResponse.add(ExecutionLogEntryResponse.create(
                        log.result(),
                        log.message(),
                        log.createdAt()
                ));
            }
        }

        return ScheduledReportEntryResponse.create(
                x.name().split("-")[1],
                x.name(),
                x.createdAt(),
                x.nextFireTime(),
                x.previousFireTime(),
                x.triggerState(),
                x.cronExpression(),
                cronDescriptor.describe(cronParser.parse(x.cronExpression())),
                nzyme.getSchedulingService().findEmailReceiversOfReport(x.name()),
                executionLogResponse
        );
    }

}
