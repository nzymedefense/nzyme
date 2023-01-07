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

package app.nzyme.core.reporting;

import app.nzyme.core.NzymeNode;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import app.nzyme.core.configuration.ReportingConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.*;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import javax.mail.util.ByteArrayDataSource;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class ReportJob implements Job {

    private static final Logger LOG = LogManager.getLogger(ReportJob.class);

    protected static final DateTimeFormatter LONG_DATETIME = DateTimeFormat.forPattern("MMMM dd, yyyy, HH:mm:ss aa (Z ZZZZ)");
    protected static final DateTimeFormatter LONG_DATETIME_LESS_ZONE = DateTimeFormat.forPattern("MMMM dd, yyyy, HH:mm:ss aa (Z)");

    private final Configuration templateConfig;

    public ReportJob() {
        // Set up template engine.
        this.templateConfig = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_30);
        this.templateConfig.setClassForTemplateLoading(this.getClass(), "/");
        this.templateConfig.setDefaultEncoding("UTF-8");
        this.templateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.templateConfig.setLogTemplateExceptions(false);
        this.templateConfig.setWrapUncheckedExceptions(true);
        this.templateConfig.setFallbackOnNullLoopVariable(false);
    }

    protected Configuration getTemplateConfig() {
        return templateConfig;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        NzymeNode nzyme = null;
        String jobName = null;
        List<String> emailReceivers;

        try {
            SchedulerContext schedContext = context.getScheduler().getContext();
            nzyme = (NzymeNode) schedContext.get("nzyme");

            jobName = context.getJobDetail().getKey().getName();
            emailReceivers = nzyme.getSchedulingService().findEmailReceiversOfReport(jobName);
        } catch (SchedulerException e) {
            if (nzyme != null) {
                nzyme.getSchedulingService().logReportExecutionResult(
                        jobName,
                        ReportBase.EXECUTION_RESULT.ERROR,
                        "Could not initialize report. [" + e.getMessage() + "] - See nzyme log for more details.",
                        null
                );
            }
            throw new JobExecutionException(e);
        }

        try {
            String reportContent = runReport(nzyme, emailReceivers);

            if (emailReceivers != null && !emailReceivers.isEmpty()) {
                if (nzyme.getConfiguration().reporting().email() == null) {
                    LOG.error("Report has email receivers configured but reporting has no SMTP settings. Please refer to the documentation. Cannot send emails.");
                } else {
                    LOG.debug("Sending report email to: {}", emailReceivers);

                    ReportingConfiguration.Email emailConfig = nzyme.getConfiguration().reporting().email();
                    Mailer mailer = MailerBuilder
                            .withSMTPServer(emailConfig.host(), emailConfig.port(), emailConfig.username(), emailConfig.password())
                            .withTransportStrategy(emailConfig.transportStrategy())
                            .clearEmailAddressCriteria()
                            .buildMailer();

                    LOG.info("Sending report emails.");
                    for (String receiver : emailReceivers) {
                        LOG.debug("Sending report email to <{}>", receiver);
                        try {
                            Email email = EmailBuilder.startingBlank()
                                    .to(receiver)
                                    .from(emailConfig.from())
                                    .withSubject(emailConfig.subjectPrefix() + " Report: " + getName())
                                    .withPlainText("Report is attached. - Nzyme.\n\n(Download and open in browser for best display quality.)")
                                    .withAttachment("report.html", new ByteArrayDataSource(reportContent.getBytes(StandardCharsets.UTF_8), "text/html"))
                                    .buildEmail();

                            mailer.sendMail(email);
                        } catch(Exception e) {
                            LOG.error("Could not send Email.", e);
                        }
                    }
                }
            } else {
                LOG.info("Report has no email receivers configured.");
            }

            nzyme.getSchedulingService().logReportExecutionResult(
                    jobName,
                    ReportBase.EXECUTION_RESULT.SUCCESS,
                    "Report executed successfully.",
                    reportContent
            );
        } catch(Exception e) {
            LOG.error("Could not execute report", e);

            nzyme.getSchedulingService().logReportExecutionResult(
                    jobName,
                    ReportBase.EXECUTION_RESULT.ERROR,
                    "Could not execute report. [" + e.getMessage() + "] - See nzyme log for more details.",
                    null
            );

            throw e;
        }
    }

    public abstract String runReport(NzymeNode nzyme, List<String> emailReceivers) throws JobExecutionException;
    public abstract String getName();

}
