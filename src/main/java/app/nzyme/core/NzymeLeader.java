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

package app.nzyme.core;

import app.nzyme.plugin.*;
import app.nzyme.plugin.retro.RetroService;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.nzyme.core.alerts.service.AlertsService;
import app.nzyme.core.bandits.engine.ContactManager;
import app.nzyme.core.bandits.trackers.GroundStation;
import app.nzyme.core.bandits.trackers.TrackerManager;
import app.nzyme.core.configuration.db.BaseConfigurationService;
import app.nzyme.core.configuration.leader.LeaderConfiguration;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.clients.Clients;
import app.nzyme.core.dot11.networks.sentry.Sentry;
import app.nzyme.core.dot11.probes.Dot11Probe;
import app.nzyme.core.dot11.networks.Networks;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.events.EventService;
import app.nzyme.core.notifications.Uplink;
import app.nzyme.core.ouis.OUIManager;
import app.nzyme.core.processing.FrameProcessor;
import app.nzyme.core.scheduler.SchedulingService;
import app.nzyme.core.systemstatus.SystemStatus;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.taps.TapManager;

import java.security.Key;
import java.util.List;
import java.util.Optional;

public interface NzymeLeader extends RemoteConnector, PluginEntryPoint, DatabaseProvider, NodeIdProvider, MetricsRegistryProvider {

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

    List<String> getInitializedPlugins();

    Optional<RetroService> retroService();

    Crypto getCrypto();

    ObjectMapper getObjectMapper();

    Registry getDatabaseCoreRegistry();

    Key getSigningKey();
    Version getVersion();

}
