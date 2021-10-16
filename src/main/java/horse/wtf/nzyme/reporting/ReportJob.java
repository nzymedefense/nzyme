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

package horse.wtf.nzyme.reporting;

import horse.wtf.nzyme.NzymeLeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;

import java.util.List;

public abstract class ReportJob implements Job {

    private static final Logger LOG = LogManager.getLogger(ReportJob.class);

    public abstract String runReport(NzymeLeader nzyme, List<String> emailReceivers) throws JobExecutionException;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        NzymeLeader nzyme = null;
        String jobName = null;
        List<String> emailReceivers;

        try {
            SchedulerContext schedContext = context.getScheduler().getContext();
            nzyme = (NzymeLeader) schedContext.get("nzyme");

            jobName = context.getJobDetail().getKey().getName();
            emailReceivers = nzyme.getSchedulingService().findEmailReceiversOfReport(jobName);
        } catch (SchedulerException e) {
            if (nzyme != null) {
                nzyme.getSchedulingService().logReportExecutionResult(
                        jobName,
                        Report.EXCECUTION_RESULT.ERROR,
                        "Could not initialize report. [" + e.getMessage() + "] - See nzyme log for more details.",
                        null
                );
            }
            throw new JobExecutionException(e);
        }

        try {
            String reportContent = runReport(nzyme, emailReceivers);

            nzyme.getSchedulingService().logReportExecutionResult(
                    jobName,
                    Report.EXCECUTION_RESULT.SUCCESS,
                    "Report executed successfully.",
                    reportContent
            );
        } catch(Exception e) {
            LOG.error("Could not execute report", e);

            nzyme.getSchedulingService().logReportExecutionResult(
                    jobName,
                    Report.EXCECUTION_RESULT.ERROR,
                    "Could not execute report. [" + e.getMessage() + "] - See nzyme log for more details.",
                    null
            );

            throw e;
        }
    }

}
