/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.periodicals.alerting.beaconrate;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.alerts.AlertsService;
import horse.wtf.nzyme.alerts.BeaconRateAnomalyAlert;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.configuration.Dot11NetworkDefinition;
import horse.wtf.nzyme.dot11.networks.BSSID;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.dot11.networks.SSID;
import horse.wtf.nzyme.dot11.networks.beaconrate.BeaconRate;
import horse.wtf.nzyme.periodicals.Periodical;
import horse.wtf.nzyme.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BeaconRateAnomalyAlertMonitor extends Periodical {

    private static final Logger LOG = LogManager.getLogger(BeaconRateAnomalyAlertMonitor.class);

    private final Networks networks;
    private final Configuration configuration;
    private final AlertsService alertsService;

    private final Timer timer;

    public BeaconRateAnomalyAlertMonitor(Nzyme nzyme) {
        this.networks = nzyme.getNetworks();
        this.configuration = nzyme.getConfiguration();
        this.alertsService = nzyme.getAlertsService();

        this.timer = nzyme.getMetrics().timer(MetricRegistry.name(MetricNames.BEACON_RATE_MONITOR_TIMER));
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
                                ssid.name(),
                                bssid.bssid(),
                                beaconRate.rate(),
                                network.beaconRate())
                        );
                    }
                }
            }
        } catch(Exception e) {
            LOG.error("Could not check becon rates for alerting.", e);
        } finally {
            ctx.stop();
        }

    }

    @Override
    public String getName() {
        return "BeaconRateAnomalyAlertMonitor";
    }

}
