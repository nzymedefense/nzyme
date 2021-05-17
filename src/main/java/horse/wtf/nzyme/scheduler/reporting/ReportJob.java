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

package horse.wtf.nzyme.scheduler.reporting;

import horse.wtf.nzyme.NzymeLeader;
import org.quartz.*;

public abstract class ReportJob implements Job {

    public abstract void runReport(NzymeLeader nzyme) throws JobExecutionException;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        NzymeLeader nzyme;
        try {
            SchedulerContext schedContext = context.getScheduler().getContext();
            nzyme = (NzymeLeader) schedContext.get("nzyme");
        } catch (SchedulerException e) {
            throw new JobExecutionException(e);
        }

        if (nzyme == null) {
            throw new JobExecutionException("Could not retrieve nzyme from scheduler context.");
        }

        runReport(nzyme);
    }

}
