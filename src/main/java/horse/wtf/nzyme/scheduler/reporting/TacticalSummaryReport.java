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

import com.google.common.collect.Maps;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import horse.wtf.nzyme.NzymeLeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.quartz.*;

import javax.annotation.Nullable;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;

public class TacticalSummaryReport implements Report {

    private static final Logger LOG = LogManager.getLogger(TacticalSummaryReport.class);

    private final ScheduleBuilder<? extends Trigger> schedule;

    public TacticalSummaryReport(int hourOfDay, int minuteOfHour) {
        this.schedule = dailyAtHourAndMinute(hourOfDay, minuteOfHour)
                .withMisfireHandlingInstructionFireAndProceed(); // On misfire, fire immediately, then back to schedule.
    }

    @Override
    public ScheduleBuilder<? extends Trigger> getSchedule() {
        return schedule;
    }

    @Override
    public String getName() {
        return "Tactical Summary";
    }

    @Override
    public Class<? extends Job> getJobClass() {
        return Report.class;
    }

    public static final class Report extends ReportJob {

        private final Configuration templateConfig;

        public Report() {
            // Set up template engine.
            this.templateConfig = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_30);
            this.templateConfig.setClassForTemplateLoading(this.getClass(), "/");
            this.templateConfig.setDefaultEncoding("UTF-8");
            this.templateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            this.templateConfig.setLogTemplateExceptions(false);
            this.templateConfig.setWrapUncheckedExceptions(true);
            this.templateConfig.setFallbackOnNullLoopVariable(false);
        }

        public void runReport(NzymeLeader nzyme, @Nullable Writer writer) throws JobExecutionException {
            try {
                Map<String, Object> parameters = Maps.newHashMap();
                parameters.put("document_title", "nzyme - Tactical Summary Report");
                parameters.put("generated_at", DateTime.now().toString());

                Template template = this.templateConfig.getTemplate("reports/tactical_summary_report.ftl");

                if (writer != null) {
                    template.process(parameters, writer);
                } else {
                    // email
                }

                // store in DB
                // ...
            } catch(Exception e) {
                throw new JobExecutionException("Could not create report content.", e);
            }
        }

        @Override
        public void runReport(NzymeLeader nzyme) throws JobExecutionException {
            runReport(nzyme, null);
        }

    }

}

