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

package horse.wtf.nzyme;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.alerts.AlertsService;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.dot11.clients.Clients;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.ouis.OUIManager;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.systemstatus.SystemStatus;

import java.util.Collections;
import java.util.List;

public class MockNzyme implements Nzyme {

    private final Statistics statistics;
    private final SystemStatus systemStatus;
    private final Networks networks;
    private final OUIManager ouiManager;
    private final MetricRegistry metricRegistry;
    private final AlertsService alertsService;

    public MockNzyme() {
        this.metricRegistry = new MetricRegistry();
        this.statistics = new Statistics();
        this.systemStatus = new SystemStatus();
        this.networks = new Networks(this);
        this.ouiManager = new OUIManager(this);
        this.alertsService = new AlertsService(this);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Networks getNetworks() {
        return networks;
    }

    @Override
    public Clients getClients() {
        return null;
    }

    @Override
    public Statistics getStatistics() {
        return statistics;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public MetricRegistry getMetrics() {
        return metricRegistry;
    }

    @Override
    public Database getDatabase() {
        return null;
    }

    @Override
    public List<Dot11Probe> getProbes() {
        return Collections.emptyList();
    }

    @Override
    public AlertsService getAlertsService() {
        return alertsService;
    }

    @Override
    public SystemStatus getSystemStatus() {
        return systemStatus;
    }

    @Override
    public OUIManager getOUIManager() {
        return ouiManager;
    }

}
