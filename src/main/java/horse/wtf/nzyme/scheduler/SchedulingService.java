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

package horse.wtf.nzyme.scheduler;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.scheduler.reporting.Report;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class SchedulingService {

    public enum SCHEDULER_GROUP {
        REPORTS
    }

    public enum TRIGGER_GROUP {
        REPORTS
    }

    private final Scheduler scheduler;

    public SchedulingService(NzymeLeader nzyme) throws SchedulerException {
        Properties config = new Properties();
        config.setProperty("org.quartz.scheduler.instanceName", "NzymeScheduler");
        config.setProperty("org.quartz.threadPool.threadCount", "15");
        config.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        config.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        config.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        config.setProperty("org.quartz.jobStore.tablePrefix", "scheduler_");
        config.setProperty("org.quartz.jobStore.dataSource", "nzymepsql");
        config.setProperty("org.quartz.dataSource.nzymepsql.driver", "org.postgresql.Driver");
        config.setProperty("org.quartz.dataSource.nzymepsql.URL", "jdbc:" + nzyme.getConfiguration().databasePath());

        this.scheduler = new StdSchedulerFactory(config).getScheduler();
        this.scheduler.getContext().put("nzyme", nzyme);
    }

    public void initialize() throws SchedulerException {
        this.scheduler.start();
    }

    public void scheduleReport(Report report) throws SchedulerException {
        // todo support multiple keys etc. this has to come out of DB with CRUD

        JobDetail job = newJob(report.getJobClass())
                .withIdentity(report.getName(), SCHEDULER_GROUP.REPORTS.toString())
                .build();

        Trigger trigger = newTrigger()
                .withIdentity(report.getName() + "-cron", TRIGGER_GROUP.REPORTS.toString())
                .startNow()
                .withSchedule(report.getSchedule())
                .build();

        this.scheduler.scheduleJob(job, trigger);
    }

}
