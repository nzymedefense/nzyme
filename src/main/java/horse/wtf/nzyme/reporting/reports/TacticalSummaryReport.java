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

package horse.wtf.nzyme.reporting.reports;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.dot11.networks.sentry.db.SentrySSID;
import horse.wtf.nzyme.reporting.Report;
import horse.wtf.nzyme.reporting.ReportJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.*;

import javax.annotation.Nullable;
import java.io.Writer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;

public class TacticalSummaryReport implements Report {

    private static final Logger LOG = LogManager.getLogger(TacticalSummaryReport.class);

    private static final DateTimeFormatter LONG_DATETIME = DateTimeFormat.forPattern("MMMM dd, yyyy, HH:mm:ss aa (Z ZZZZ)");
    private static final DateTimeFormatter LONG_DATETIME_LESS_ZONE = DateTimeFormat.forPattern("MMMM dd, yyyy, HH:mm:ss aa (Z)");

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
                parameters.put("title", "nzyme - Tactical Summary Report");
                parameters.put("generated_at", DateTime.now().toString(LONG_DATETIME));
                parameters.put("networks", buildNetworks(nzyme));
                parameters.put("alerts", buildAlerts(nzyme));

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

        private List<Map<String, String>> buildAlerts(NzymeLeader nzyme) {
            List<Map<String, String>> result = Lists.newArrayList();

            for (Alert alert : nzyme.getAlertsService().findAllAlertsSince24HoursAgo(100).values()) {
                Map<String, String> alertData = Maps.newHashMap();
                alertData.put("type", alert.getType().toString());
                alertData.put("first_seen", alert.getFirstSeen().withZone(DateTimeZone.getDefault()).toString(LONG_DATETIME_LESS_ZONE));
                alertData.put("last_seen", alert.getLastSeen().withZone(DateTimeZone.getDefault()).toString(LONG_DATETIME_LESS_ZONE));
                alertData.put("frames", alert.isUseFrameCount() ? alert.getFrameCount().toString() : "n/a");
                result.add(alertData);
            }

            return result;
        }

        private List<Map<String, Object>> buildNetworks(NzymeLeader nzyme) {
            List<Map<String, Object>> result = Lists.newArrayList();

            ImmutableList.Builder<String> ssids = new ImmutableList.Builder<>();
            for (SentrySSID newSSID :nzyme.getSentry().findNewToday()){
                if (!Strings.isNullOrEmpty(newSSID.ssid())) {
                    ssids.add(newSSID.ssid());
                }
            }

            ImmutableList<String> newNetworks = ssids.build();

            List<SentrySSID> sorted = nzyme.getSentry().findSeenToday()
                    .stream()
                    .sorted(Comparator.comparing(SentrySSID::ssid, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            for (SentrySSID network : sorted) {
                if(Strings.isNullOrEmpty(network.ssid())) {
                    continue;
                }

                Map<String, Object> ssidData = Maps.newHashMap();
                ssidData.put("ssid", network.ssid());
                ssidData.put("first_seen", network.firstSeen().toString(LONG_DATETIME_LESS_ZONE));
                ssidData.put("last_seen", network.lastSeen().toString(LONG_DATETIME_LESS_ZONE));
                ssidData.put("new_today", newNetworks.contains(network.ssid()));
                result.add(ssidData);
            }

            return result;
        }

        @Override
        public void runReport(NzymeLeader nzyme) throws JobExecutionException {
            runReport(nzyme, null);
        }

    }
}