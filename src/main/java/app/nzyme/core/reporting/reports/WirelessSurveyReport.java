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

package app.nzyme.core.reporting.reports;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import freemarker.template.Template;
import app.nzyme.core.NzymeLeader;
import app.nzyme.core.dot11.networks.sentry.db.SentrySSID;
import app.nzyme.core.reporting.ReportBase;
import app.nzyme.core.reporting.ReportJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionException;

import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WirelessSurveyReport extends ReportBase {

    private static final Logger LOG = LogManager.getLogger(WirelessSurveyReport.class);

    public static final String NAME = "Wireless Survey";

    public WirelessSurveyReport(int hourOfDay, int minuteOfHour) {
        super(hourOfDay, minuteOfHour);
    }

    public String getName() {
        return NAME;
    }

    public Class<? extends Job> getJobClass() {
        return Report.class;
    }

    public static final class Report extends ReportJob {

        @Override
        public String runReport(NzymeLeader nzyme, List<String> emailReceivers) throws JobExecutionException {
            try {
                Map<String, Object> parameters = Maps.newHashMap();
                parameters.put("title", "nzyme - " + NAME);
                parameters.put("time_range", "Previous 24 hours");
                parameters.put("generated_at", DateTime.now().toString(LONG_DATETIME));
                parameters.put("networks", buildNetworks(nzyme));

                Template template = getTemplateConfig().getTemplate("reports/wireless_survey_report.ftl");

                StringWriter writer = new StringWriter();
                template.process(parameters, writer);
                return writer.toString();
            } catch(Exception e) {
                throw new JobExecutionException("Could not create report content.", e);
            }
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
        public String getName() {
            return NAME;
        }
    }

}
