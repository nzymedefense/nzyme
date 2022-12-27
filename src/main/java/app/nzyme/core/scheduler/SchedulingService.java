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

package app.nzyme.core.scheduler;

import app.nzyme.core.NzymeLeader;
import app.nzyme.core.reporting.ReportBase;
import app.nzyme.core.reporting.db.ExecutionLogEntry;
import app.nzyme.core.reporting.db.ScheduledReportEntry;
import org.joda.time.DateTime;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
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

    public String scheduleReport(ReportBase report) throws SchedulerException {
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

        nzyme.getDatabase().withHandle(handle ->
                handle.createUpdate("INSERT INTO report_metadata(report_name, created_at) VALUES(:reportName, :createdAt)")
                        .bind("reportName", reportName)
                        .bind("createdAt", DateTime.now())
                        .execute()
        );

        return reportName;
    }

    public void unscheduleAndDeleteReport(String reportName) throws SchedulerException {
        this.scheduler.deleteJob(JobKey.jobKey(reportName, SCHEDULER_GROUP.REPORTS.toString()));

        // TODO use constraints
        nzyme.getDatabase().withHandle(handle ->
                handle.createUpdate("DELETE FROM report_metadata WHERE report_name = :reportName")
                        .bind("reportName", reportName)
                        .execute()
        );

        nzyme.getDatabase().withHandle(handle ->
                handle.createUpdate("DELETE FROM report_receivers_email WHERE report_name = :reportName")
                        .bind("reportName", reportName)
                        .execute()
        );

        nzyme.getDatabase().withHandle(handle ->
                handle.createUpdate("DELETE FROM report_execution_log WHERE report_name = :reportName")
                        .bind("reportName", reportName)
                        .execute()
        );
    }

    public List<ScheduledReportEntry> findAllScheduledReports() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT t.job_name, t.next_fire_time, t.prev_fire_time, t.trigger_state, c.cron_expression, m.created_at " +
                        "FROM scheduler_triggers t LEFT JOIN scheduler_cron_triggers AS c ON c.trigger_name = t.trigger_name " +
                        "LEFT JOIN report_metadata AS m ON m.report_name = t.trigger_name " +
                         "WHERE t.trigger_group = 'REPORTS' AND t.trigger_type = 'CRON';")
                        .mapTo(ScheduledReportEntry.class)
                        .list()
        );
    }

    public Optional<ScheduledReportEntry> findScheduledReport(String reportName) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT t.job_name, t.next_fire_time, t.prev_fire_time, t.trigger_state, c.cron_expression, m.created_at " +
                                "FROM scheduler_triggers t LEFT JOIN scheduler_cron_triggers AS c ON c.trigger_name = t.trigger_name " +
                                "LEFT JOIN report_metadata AS m ON m.report_name = t.trigger_name " +
                                "WHERE t.trigger_group = 'REPORTS' AND t.trigger_type = 'CRON' AND t.job_name = :reportName")
                        .bind("reportName", reportName)
                        .mapTo(ScheduledReportEntry.class)
                        .findFirst()
        );
    }

    public List<String> findEmailReceiversOfReport(String reportName) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT address FROM report_receivers_email WHERE report_name = :reportName")
                        .bind("reportName", reportName)
                        .mapTo(String.class)
                        .list()
        );
    }

    public void addEmailReceiverToReport(String reportName, String emailAddress) {
        nzyme.getDatabase().withHandle(handle ->
                handle.createUpdate("INSERT INTO report_receivers_email(report_name, address) VALUES(:reportName, :emailAddress)")
                        .bind("reportName", reportName)
                        .bind("emailAddress", emailAddress)
                        .execute()
        );
    }

    public void removeEmailReceiverFromReport(String reportName, String emailAddress) {
        nzyme.getDatabase().withHandle(handle ->
                handle.createUpdate("DELETE FROM report_receivers_email WHERE report_name = :reportName AND address = :emailAddress")
                        .bind("reportName", reportName)
                        .bind("emailAddress", emailAddress)
                        .execute()
        );
    }

    public void logReportExecutionResult(String reportName, ReportBase.EXECUTION_RESULT result, String message, @Nullable String reportContent) {
        nzyme.getDatabase().withHandle(handle ->
                handle.createUpdate("INSERT INTO report_execution_log(report_name, result, message, content, created_at) " +
                                "VALUES(:reportName, :result, :message, :content, :createdAt)")
                        .bind("reportName", reportName)
                        .bind("result", result)
                        .bind("message", message)
                        .bind("content", reportContent)
                        .bind("createdAt", DateTime.now())
                        .execute()
        );
    }

    public List<ExecutionLogEntry> findExecutionLogs(String reportName) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id, report_name, result, message, content, created_at FROM report_execution_log WHERE report_name = :reportName LIMIT 14")
                        .bind("reportName", reportName)
                        .mapTo(ExecutionLogEntry.class)
                        .list()
        );
    }

    public Optional<ExecutionLogEntry> findExecutionLog(String reportName, Long executionId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id, report_name, result, message, content, created_at FROM report_execution_log WHERE report_name = :reportName AND id = :id")
                        .bind("id", executionId)
                        .bind("reportName", reportName)
                        .mapTo(ExecutionLogEntry.class)
                        .findFirst()
        );
    }

}
