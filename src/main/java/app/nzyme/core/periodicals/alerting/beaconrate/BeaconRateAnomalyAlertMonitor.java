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

package app.nzyme.core.periodicals.alerting.beaconrate;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import app.nzyme.core.NzymeLeader;
import app.nzyme.core.alerts.service.AlertsService;
import app.nzyme.core.alerts.BeaconRateAnomalyAlert;
import app.nzyme.core.configuration.leader.LeaderConfiguration;
import app.nzyme.core.configuration.Dot11NetworkDefinition;
import app.nzyme.core.dot11.networks.BSSID;
import app.nzyme.core.dot11.networks.Networks;
import app.nzyme.core.dot11.networks.SSID;
import app.nzyme.core.dot11.networks.beaconrate.BeaconRate;
import app.nzyme.core.periodicals.Periodical;
import app.nzyme.core.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class BeaconRateAnomalyAlertMonitor extends Periodical {

    private static final Logger LOG = LogManager.getLogger(BeaconRateAnomalyAlertMonitor.class);

    private final Networks networks;
    private final LeaderConfiguration configuration;
    private final AlertsService alertsService;

    private final Timer timer;

    public BeaconRateAnomalyAlertMonitor(NzymeLeader nzyme) {
        this.networks = nzyme.getNetworks();
        this.configuration = nzyme.getConfiguration();
        this.alertsService = nzyme.getAlertsService();

        this.timer = nzyme.getMetrics().timer(MetricRegistry.name(MetricNames.BEACON_RATE_MONITOR_TIMING));
    }

    @Override
    protected void execute() {
        Timer.Context ctx = this.timer.time();

        try {
            for (BSSID bssid : networks.getBSSIDs().values()) {
                for (SSID ssid : bssid.ssids().values()) {
                    if (!ssid.isHumanReadable()) {
                        continue;
                    }

                    // Only run for our own networks.
                    if (!configuration.ourSSIDs().contains(ssid.name())) {
                        continue;
                    }

                    Dot11NetworkDefinition network = configuration.findNetworkDefinition(bssid.bssid(), ssid.name());
                    if (network == null) {
                        continue;
                    }

                    BeaconRate beaconRate = ssid.beaconRate();
                    if (beaconRate == null || beaconRate.rate() == null) {
                        continue;
                    }

                    if (beaconRate.rate() > network.beaconRate()) {
                        alertsService.handle(BeaconRateAnomalyAlert.create(
                                DateTime.now(),
                                ssid.name(),
                                bssid.bssid(),
                                beaconRate.rate(),
                                network.beaconRate())
                        );
                    }
                }
            }
        } catch(Exception e) {
            LOG.error("Beacon Rate Monitor run failed", e);
        } finally {
            ctx.stop();
        }

    }

    @Override
    public String getName() {
        return "BeaconRateAnomalyAlertMonitor";
    }

}
