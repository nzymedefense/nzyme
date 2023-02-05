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

import app.nzyme.core.distributed.ClusterManager;
import app.nzyme.core.distributed.NodeManager;
import app.nzyme.core.monitoring.health.HealthMonitor;
import app.nzyme.plugin.Database;
import app.nzyme.plugin.NodeIdentification;
import app.nzyme.plugin.Registry;
import app.nzyme.plugin.retro.RetroService;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import app.nzyme.core.alerts.Alert;
import app.nzyme.core.alerts.service.AlertsService;
import app.nzyme.core.bandits.engine.ContactManager;
import app.nzyme.core.bandits.trackers.GroundStation;
import app.nzyme.core.bandits.trackers.TrackerManager;
import app.nzyme.core.configuration.IncompleteConfigurationException;
import app.nzyme.core.configuration.InvalidConfigurationException;
import app.nzyme.core.configuration.db.BaseConfigurationService;
import app.nzyme.core.configuration.node.NodeConfiguration;
import app.nzyme.core.configuration.node.NodeConfigurationLoader;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.clients.Clients;
import app.nzyme.core.dot11.frames.Dot11Frame;
import app.nzyme.core.dot11.networks.sentry.Sentry;
import app.nzyme.core.dot11.probes.Dot11Probe;
import app.nzyme.core.dot11.networks.Networks;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.notifications.Notification;
import app.nzyme.core.notifications.Uplink;
import app.nzyme.core.ouis.OUIManager;
import app.nzyme.core.processing.FrameProcessor;
import app.nzyme.core.remote.forwarders.Forwarder;
import app.nzyme.core.scheduler.SchedulingService;
import app.nzyme.core.systemstatus.SystemStatus;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.taps.TapManager;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import liquibase.exception.LiquibaseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Path;
import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MockNzyme implements NzymeNode {

    private File loadFromResourceFile(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        if (resource == null) {
            throw new RuntimeException("test config file does not exist in resources");
        }

        return new File(resource.getFile());
    }

    private final NodeManager nodeManager;

    private final NodeIdentification nodeIdentification;

    private final NodeConfiguration configuration;
    private final SystemStatus systemStatus;
    private final Networks networks;
    private final Clients clients;
    private final OUIManager ouiManager;
    private final MetricRegistry metricRegistry;
    private final AlertsService alertsService;
    private final ContactManager contactManager;
    private final Key signingKey;
    private final ObjectMapper objectMapper;
    private final MemoryRegistry memoryRegistry;
    private final Version version;
    private final Database database;
    private final List<Uplink> uplinks;
    private final List<Forwarder> forwarders;
    private final FrameProcessor frameProcessor;
    private final Anonymizer anonymizer;
    private final Sentry sentry;
    private final BaseConfigurationService configurationService;
    private final Path dataDirectory;

    private final Crypto crypto;

    public MockNzyme() {
        this(0);
    }

    public MockNzyme(int sentryInterval) {
        this.version = new Version();

        try {
            String configFile = "nzyme-test-complete-valid.conf.test";
            if (System.getProperty("os.name").startsWith("Windows")) {
                configFile = "nzyme-test-complete-valid-windows.conf.test";
                System.out.println("loading Windows nzyme configuration file");
            }

            this.configuration = new NodeConfigurationLoader(loadFromResourceFile(configFile), false).get();
            this.dataDirectory = Path.of("test_data_dir");
        } catch (InvalidConfigurationException | IncompleteConfigurationException | FileNotFoundException e) {
            throw new RuntimeException("Could not load test config file from resources.", e);
        }
        this.database = new DatabaseImpl(configuration);

        try {
            this.database.initializeAndMigrate();
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        }

        this.nodeManager = new NodeManager(this);
        try {
            this.nodeManager.initialize();
        } catch (NodeManager.NodeInitializationException e) {
            throw new RuntimeException(e);
        }

        this.nodeIdentification = NodeIdentification.create(nodeManager.getLocalNodeId(), "mocky-mock");

        this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);


        this.uplinks = Lists.newArrayList();
        this.forwarders = Lists.newArrayList();

        this.frameProcessor = new FrameProcessor();

        this.database.useHandle(handle -> handle.execute("TRUNCATE sentry_ssids"));

        this.configurationService = new BaseConfigurationService(this);
        this.configurationService.initialize();

        this.metricRegistry = new MetricRegistry();
        this.crypto = new Crypto(this);

        // Register JVM metrics.
        this.metricRegistry.register("gc", new GarbageCollectorMetricSet());
        this.metricRegistry.register("classes", new ClassLoadingGaugeSet());
        this.metricRegistry.register("fds", new FileDescriptorRatioGauge());
        this.metricRegistry.register("jvm", new JvmAttributeGaugeSet());
        this.metricRegistry.register("mem", new MemoryUsageGaugeSet());
        this.metricRegistry.register("threadstates", new ThreadStatesGaugeSet());

        this.memoryRegistry = new MemoryRegistry();
        this.systemStatus = new SystemStatus();
        this.networks = new Networks(this);
        this.clients = new Clients(this);
        this.ouiManager = new OUIManager(this);
        this.alertsService = new AlertsService(this);
        this.objectMapper = new ObjectMapper();
        this.contactManager = new ContactManager(this);
        this.anonymizer = new Anonymizer(false, "/tmp");

        if (sentryInterval == 0) {
            this.sentry = null;
        } else {
            this.sentry = new Sentry(this, sentryInterval);
        }
    }

    @Override
    public void initialize() {
        try {
            this.crypto.initialize();
        } catch (Crypto.CryptoInitializationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public NodeManager getNodeManager() {
        return nodeManager;
    }

    @Override
    public ClusterManager getClusterManager() {
        return null;
    }

    @Override
    public HealthMonitor getHealthMonitor() {
        return null;
    }

    @Override
    public Ethernet getEthernet() {
        return null;
    }

    @Override
    public FrameProcessor getFrameProcessor() {
        return frameProcessor;
    }

    @Override
    public Networks getNetworks() {
        return networks;
    }

    @Override
    public Sentry getSentry() {
        return sentry;
    }

    @Override
    public Clients getClients() {
        return clients;
    }

    @Override
    public void registerUplink(Uplink uplink) {
        this.uplinks.add(uplink);
    }

    @Override
    public void notifyUplinks(Notification notification, Dot11MetaInformation meta) {
        for (Uplink uplink : uplinks) {
            uplink.notify(notification, meta);
        }
    }

    @Override
    public void notifyUplinksOfAlert(Alert alert) {
        for (Uplink uplink : uplinks) {
            uplink.notifyOfAlert(alert);
        }
    }

    @Override
    public void forwardFrame(Dot11Frame frame) {
        for (Forwarder forwarder : forwarders) {
            forwarder.forward(frame);
        }

    }

    @Override
    public NodeConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public BaseConfigurationService getConfigurationService() {
        return configurationService;
    }

    @Override
    public Path getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public MetricRegistry getMetrics() {
        return metricRegistry;
    }

    @Override
    public MemoryRegistry getRegistry() {
        return memoryRegistry;
    }

    @Override
    public Database getDatabase() {
        return database;
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
    public ContactManager getContactManager() {
        return contactManager;
    }

    @Override
    public TapManager getTapManager() {
        return null;
    }

    @Override
    public List<String> getIgnoredFingerprints() {
        return Collections.emptyList();
    }

    @Override
    public void registerIgnoredFingerprint(String fingerprint) {

    }

    @Override
    public TablesService getTablesService() {
        return null;
    }

    @Override
    public TrackerManager getTrackerManager() {
        return null;
    }

    @Override
    public GroundStation getGroundStation() {
        return null;
    }

    @Override
    public SystemStatus getSystemStatus() {
        return systemStatus;
    }

    @Override
    public SchedulingService getSchedulingService() {
        return null;
    }

    @Override
    public OUIManager getOUIManager() {
        return ouiManager;
    }

    @Override
    public Anonymizer getAnonymizer() {
        return anonymizer;
    }

    @Override
    public List<String> getInitializedPlugins() {
        return null;
    }

    @Override
    public Optional<RetroService> retroService() {
        return Optional.empty();
    }

    @Override
    public Crypto getCrypto() {
        return crypto;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public Registry getDatabaseCoreRegistry() {
        return null;
    }

    @Override
    public Key getSigningKey() {
        return signingKey;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public void registerRetroService(RetroService retroService) {
    }

    @Override
    public void registerRestResource(Object o) {

    }

    public Registry getRegistry(String s) {
        return null;
    }

    @Override
    public NodeIdentification getNodeInformation() {
        return nodeIdentification;
    }
}
