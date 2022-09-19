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

import app.nzyme.plugin.Database;
import app.nzyme.plugin.DatabaseProvider;
import app.nzyme.plugin.NodeIdProvider;
import app.nzyme.plugin.PluginEntryPoint;
import app.nzyme.plugin.retro.RetroService;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.bandits.engine.ContactManager;
import horse.wtf.nzyme.bandits.trackers.GroundStation;
import horse.wtf.nzyme.bandits.trackers.TrackerManager;
import horse.wtf.nzyme.configuration.db.BaseConfigurationService;
import horse.wtf.nzyme.configuration.leader.LeaderConfiguration;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.clients.Clients;
import horse.wtf.nzyme.dot11.networks.sentry.Sentry;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.ethernet.Ethernet;
import horse.wtf.nzyme.events.EventService;
import horse.wtf.nzyme.notifications.Uplink;
import horse.wtf.nzyme.ouis.OUIManager;
import horse.wtf.nzyme.processing.FrameProcessor;
import horse.wtf.nzyme.scheduler.SchedulingService;
import horse.wtf.nzyme.systemstatus.SystemStatus;
import horse.wtf.nzyme.tables.TablesService;
import horse.wtf.nzyme.taps.TapManager;

import java.security.Key;
import java.util.List;
import java.util.Optional;

public interface NzymeLeader extends RemoteConnector, PluginEntryPoint, DatabaseProvider, NodeIdProvider {

    void initialize();
    void shutdown();

    String getNodeID();

    Ethernet getEthernet();

    FrameProcessor getFrameProcessor();

    Networks getNetworks();
    Sentry getSentry();
    Clients getClients();

    void registerUplink(Uplink uplink);

    LeaderConfiguration getConfiguration(); // TODO remove after switch to DB config

    BaseConfigurationService getConfigurationService();

    MetricRegistry getMetrics();

    MemoryRegistry getRegistry();

    Database getDatabase();

    List<Dot11Probe> getProbes();
    AlertsService getAlertsService();
    ContactManager getContactManager();

    TapManager getTapManager();

    List<String> getIgnoredFingerprints();
    void registerIgnoredFingerprint(String fingerprint);

    TablesService getTablesService();

    TrackerManager getTrackerManager();
    GroundStation getGroundStation();

    SystemStatus getSystemStatus();
    EventService getEventService();
    SchedulingService getSchedulingService();

    OUIManager getOUIManager();

    Anonymizer getAnonymizer();

    Optional<RetroService> retroService();

    ObjectMapper getObjectMapper();

    Key getSigningKey();
    Version getVersion();

}
