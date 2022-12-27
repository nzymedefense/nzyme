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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import freemarker.template.Template;
import app.nzyme.core.NzymeLeader;
import app.nzyme.core.configuration.Dot11BSSIDDefinition;
import app.nzyme.core.configuration.Dot11NetworkDefinition;
import app.nzyme.core.reporting.ReportBase;
import app.nzyme.core.reporting.ReportJob;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionException;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class WirelessInventoryReport extends ReportBase {

    public static final String NAME = "Wireless Inventory";

    public WirelessInventoryReport(int hourOfDay, int minuteOfHour) {
        super(hourOfDay, minuteOfHour);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
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
                parameters.put("ssids", buildAssetList(nzyme.getConfiguration().dot11Networks()));
                parameters.put("enabled_alerts", nzyme.getConfiguration().dot11Alerts());

                Template template = getTemplateConfig().getTemplate("reports/wireless_inventory_report.ftl");

                StringWriter writer = new StringWriter();
                template.process(parameters, writer);
                return writer.toString();
            } catch(Exception e) {
                throw new JobExecutionException("Could not create report content.", e);
            }
        }

        private Map<String, Object> buildAssetList(List<Dot11NetworkDefinition> networks) {
            Map<String, Object> result = Maps.newHashMap();

            for (Dot11NetworkDefinition network : networks) {
                List<Object> bssids = Lists.newArrayList();
                for (Dot11BSSIDDefinition bssid : network.bssids()) {
                    Map<String, Object> params = Maps.newHashMap();

                    params.put("bssid", bssid.address());
                    params.put("fingerprints", Joiner.on(",").join(bssid.fingerprints()));

                    bssids.add(params);
                }

                Map<String, Object> ssid = Maps.newHashMap();
                ssid.put("security", Joiner.on(",").join(network.security()));
                ssid.put("channels", Joiner.on(",").join(network.channels()));
                ssid.put("bssids", bssids);

                result.put(network.ssid(), ssid);
            }

            return result;
        }

        @Override
        public String getName() {
            return NAME;
        }

    }

}
