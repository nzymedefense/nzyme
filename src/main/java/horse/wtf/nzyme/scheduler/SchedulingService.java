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
import horse.wtf.nzyme.reporting.Report;
import horse.wtf.nzyme.reporting.db.ScheduledReportEntry;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

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
    private final NzymeLeader nzyme;

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

        this.nzyme = nzyme;
    }

    public void initialize() throws SchedulerException {
        this.scheduler.start();
    }

    public void scheduleReport(Report report) throws SchedulerException {
        // Attach a random UUID or Quartz will complain about duplicate report names.
        String reportName = report.getName() + "-" + UUID.randomUUID().toString();

        JobDetail job = newJob(report.getJobClass())
                .withIdentity(reportName, SCHEDULER_GROUP.REPORTS.toString())
                .build();

        Trigger trigger = newTrigger()
                .withIdentity(reportName, TRIGGER_GROUP.REPORTS.toString())
                .startNow()
                .withSchedule(report.getSchedule())
                .build();

        this.scheduler.scheduleJob(job, trigger);
    }

    public List<ScheduledReportEntry> findAllScheduledReports() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT t.job_name, t.next_fire_time, t.prev_fire_time, t.trigger_state, c.cron_expression " +
                        "FROM scheduler_triggers t LEFT JOIN scheduler_cron_triggers AS c ON c.trigger_name = t.trigger_name " +
                        "WHERE t.trigger_group = 'REPORTS' AND t.trigger_type = 'CRON';")
                        .mapTo(ScheduledReportEntry.class)
                        .list()
        );
    }

}
