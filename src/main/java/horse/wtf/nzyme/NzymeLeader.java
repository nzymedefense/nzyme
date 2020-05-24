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
import com.fasterxml.jackson.databind.ObjectMapper;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.bandits.engine.ContactIdentifier;
import horse.wtf.nzyme.bandits.trackers.GroundStation;
import horse.wtf.nzyme.bandits.trackers.TrackerManager;
import horse.wtf.nzyme.configuration.leader.LeaderConfiguration;
import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.clients.Clients;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.Uplink;
import horse.wtf.nzyme.ouis.OUIManager;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.systemstatus.SystemStatus;

import java.security.Key;
import java.util.List;

public interface NzymeLeader {

    void initialize();
    void shutdown();

    Networks getNetworks();
    Clients getClients();

    void registerUplink(Uplink uplink);
    void notifyUplinks(Notification notification, Dot11MetaInformation meta);
    void notifyUplinksOfAlert(Alert alert);

    Statistics getStatistics();
    LeaderConfiguration getConfiguration();

    MetricRegistry getMetrics();

    Registry getRegistry();

    Database getDatabase();

    List<Dot11Probe> getProbes();
    AlertsService getAlertsService();
    ContactIdentifier getContactIdentifier();

    TrackerManager getTrackerManager();
    GroundStation getGroundStation();

    SystemStatus getSystemStatus();

    OUIManager getOUIManager();

    ObjectMapper getObjectMapper();

    Key getSigningKey();
    Version getVersion();

}
