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

package horse.wtf.nzyme;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.bandits.engine.ContactManager;
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

public interface NzymeLeader extends UplinkHandler {

    void initialize();
    void shutdown();

    String getNodeID();

    Networks getNetworks();
    Clients getClients();

    void registerUplink(Uplink uplink);

    Statistics getStatistics();
    LeaderConfiguration getConfiguration();

    MetricRegistry getMetrics();

    Registry getRegistry();

    Database getDatabase();

    List<Dot11Probe> getProbes();
    AlertsService getAlertsService();
    ContactManager getContactManager();

    List<String> getIgnoredFingerprints();
    void registerIgnoredFingerprint(String fingerprint);

    TrackerManager getTrackerManager();
    GroundStation getGroundStation();

    SystemStatus getSystemStatus();

    OUIManager getOUIManager();

    ObjectMapper getObjectMapper();

    Key getSigningKey();
    Version getVersion();

}
