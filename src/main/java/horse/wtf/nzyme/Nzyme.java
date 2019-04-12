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

import java.util.List;

public interface Nzyme {

    void initialize();
    void shutdown();

    Networks getNetworks();
    Clients getClients();

    Statistics getStatistics();
    Configuration getConfiguration();

    MetricRegistry getMetrics();

    Database getDatabase();

    List<Dot11Probe> getProbes();
    AlertsService getAlertsService();

    SystemStatus getSystemStatus();

    OUIManager getOUIManager();

}
